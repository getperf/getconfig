package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("summary")
void Summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric("vm", json.Guest?.HostName ?: 'N/A')
    t.setMetric("numCpu", json.Config?.NumCpu ?: 'N/A')
    t.setMetric("powerState", json.Runtime?.PowerState ?: 'N/A')
    t.setMetric("memoryMB", json.Config?.MemorySizeMB ?: 'N/A')
}

@Parser("host.txt")
void Host(TestUtil t) {
    t.setMetric("hostName", t.readAll())
}

@Parser("resourceConfig")
void ResourceConfig(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def metrics = ["CpuAllocation":"numCpu",
        "MemoryAllocation":"memoryMB"]
    def headers = ['Reservation', 'Limit', 'Shares', 'OverheadLimit']
    metrics.each { key, metric ->
        def values = json.get(key)
        def row = []
        headers.each {header ->
            row << values?.get(header) ?: 'N/A'
        }
        t.devices(headers, [row], metric)
    }
}

void addIopsLimitDevice(TestUtil t, json) {
    t.metricFile = "vm_iops_limit"
    def limits = []
    def csv = []
    def headers = ["Label":"label", "Summary":"summary", "Limit":"limit", 
        "Shares":"shares", "Level": "shareLevel", 
        "Reservation":"reservation"]
    json.Hardware?.Device.each { device ->
        info = device.DeviceInfo
        iops = device.StorageIOAllocation
        shares = iops?.Shares
        if (iops) {
            def row = []
            headers.each { header, label ->
                def value
                if (shares?.get(header) != null) {
                    value = shares.get(header)
                } else if (info?.get(header) != null) {
                    value = info.get(header)
                } else if (iops?.get(header) != null) {
                    value = iops.get(header)
                } else {
                    value = 'N/A'
                }
                if (header == 'Limit') {
                    if (value == -1) {
                        value = "NoLimit"
                    } else {
                        limits << value
                    }
                }
                row << value
            }
            csv << row
        }
    }
    List headers2 = headers.values() as List<String>
    t.devices(headers2, csv)
    def status = (limits.size() == 0) ? 'NoLimit' : limits.toString()
    t.setMetric("vm_iops_limit", status)
}

void addToolStatus(TestUtil t, json) {
    def res = [:]
    res['syncTimeWithHost'] = json.Tools?.SyncTimeWithHost
    json.ExtraConfig.each { extraConfig ->
        (extraConfig.Key =~ /^vmware.tools.(.+?)$/).each { m0, m1 ->
            res[m1] = extraConfig.Value
        }
    }
    def headers = ["syncTimeWithHost", "internalversion", "requiredversion"]
    def row = headers.collect { (res.get(it) != null) ? res.get(it) : 'N/A' }
    t.devices(headers, [row], "toolsStatus")
}

void addStorageConfig(TestUtil t, disks) {
    t.metricFile = 'disk'
    def headers = ['dev', 'usage', 'datastore', 'thin', 'writeThrough', 
        'level', 'shares']
    def csv = []
    disks.each { disk ->
        def label = disk.DeviceInfo.Label
        if (!label) {
            return
        }
        label = CommonUtil.toCamelCase(label)
        def datastore = disk.Backing.FileName
        (datastore=~/\[(.+)\]/).each { m0, m1 ->
            datastore = m1
        }
        def row = [label,
            disk.DeviceInfo.Summary,
            disk.Backing.FileName,
            disk.Backing.ThinProvisioned,
            disk.Backing.WriteThrough,
            disk.Shares.Level,
            disk.Shares.Shares,
        ]
        csv << row
        // t.newMetric("disk.${label}.size", "[${label}] Size", row[1])
        // t.newMetric("disk.${label}.datastore", "[${label}] Datastore",
        //             datastore)
        // t.newMetric("disk.${label}.thin", "[${label}] ThinProvisioned",
        //             row[3])
        // t.newMetric("disk.${label}.writeThrough", "[${label}] WriteThrough",
        //             row[4])
        // t.newMetric("disk.${label}.level", "[${label}] level", row[5])
        // t.newMetric("disk.${label}.shares", "[${label}] shares", row[6])
    }
    def summary = disks*.DeviceInfo?.Summary ?: 'N/A'
    t.devices(headers, csv, "disk")
    t.results(summary.toString())
}

@Parser("config")
void Config(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json ).toPrettyString()
    // "Template": false,
    // "GuestId": "rhel6_64Guest",
    t.setMetric("template", json.Template)
    t.setMetric("guestId", json.GuestId)
    t.setMetric("memoryReserveLock", json.MemoryReservationLockedToMax)
    t.setMetric("cpuAffinity", json.CpuAffinity ?: 'N/A')
    t.setMetric("memAffinity", json.MemoryAffinity ?: 'N/A')
    t.setMetric("cpuHotAdd", json.CpuHotAddEnabled)
    t.setMetric("memHotAdd", json.MemoryHotAddEnabled)
    t.setMetric("datastore", json.DatastoreUrl*.Name.toString())
    // t.setMetric("vm_timesync", json.Tools.SyncTimeWithHost)
    // t.setMetric("vm_iops_limit", json.Hardware.Device*.StorageIOAllocation.Limit.toString())
    addIopsLimitDevice(t, json)
    addToolStatus(t, json)
    def floppyDevice
    def videoCard
    def disks = []
	json.Hardware.Device.each { node -> 
        if (node['Key'] == 8000) {  // Label:Floppy drive
            floppyDevice = node
        } else if (node['Key'] == 500) {   // Label:Video card
            videoCard = node
        } else if (node['Key'] >= 2000 && node['Key'] < 3000) {  // Label:Hard disk
            disks << node
        }
    }
    t.setMetric("floppyStartConnected", floppyDevice?.Connectable?.StartConnected?:'N/A')
    t.setMetric("floppyConnected", floppyDevice?.Connectable?.Connected?:'N/A')
    t.setMetric("videoRamKB", videoCard.VideoRamSizeInKB)
    addStorageConfig(t, disks)

    def res = [:]
    res['config'] = json.DatastoreUrl*.Name.toString()
    t.results(res)
}

@Parser("guest")
void Guest(TestUtil t) {
    t.metricFile = "net"
    def json = new JsonSlurper().parseText(t.readAll())
    def res = [:]
    def csv = []
    def headers = ["network", "ip", "netmask", "mac", "connected", 
                   "dhcp", "dnsIp", "autoConfig"]
    json.Net.each { net ->
        def label = net.Network ?: 'Default'
        def dhcp = net.IpConfig?.Dhcp
        if (dhcp) {
            dhcp = dhcp.collect { key,value -> "${key}:${value.Enable}" }
        } else {
            dhcp = 'Disable'
        }
        def row = [
            label,
            net.IpAddress ?: 'N/A',
            net.IpConfig?.IpAddress*.PrefixLength,
            net.MacAddress ?: 'N/A',
            net.Connected,
            dhcp,
            net.DnsConfig?.IpAddress ?: 'Disable',
            net.IpConfig?.AutoConfigurationEnabled ?: 'Disable',
        ]
        csv << row
        // t.newMetric("net.${label}.on", "[${label}] On", row[4])
        // t.newMetric("net.${label}.ip", "[${label}] IP", row[1])
        // t.newMetric("net.${label}.dns", "[${label}] DNS", row[6])
    }
    t.devices(headers, csv)
    t.setMetric("toolsStatus", json.ToolsStatus)
    t.results(json.IpAddress)
}

@Parser("guestHeartbeatStatus")
void GuestHeartbeatStatus(TestUtil t) {
    t.results(t.readAll())
}

