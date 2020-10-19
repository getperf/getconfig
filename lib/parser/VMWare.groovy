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

    t.results(json.Guest?.HostName ?: 'N/A')
    t.setMetric("numCpu", json.Config?.NumCpu ?: 'N/A')
    t.setMetric("powerState", json.Runtime?.PowerState ?: 'N/A')
    t.setMetric("memoryMB", json.Config?.MemorySizeMB ?: 'N/A')
    t.setMetric("hostName", json.Runtime?.Host?.Name ?: 'N/A')
}

@Parser("resourceConfig")
void ResourceConfig(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ['cpuLimit', 'cpuShareLevel', 'memLimit', 'memShareLevel']
    def row = [
        json.CpuAllocation?.Limit ?: 'N/A',
        json.CpuAllocation?.Shares?.Level ?: 'N/A',
        json.MemoryAllocation?.Limit ?: 'N/A',
        json.MemoryAllocation?.Shares?.Level ?: 'N/A'
    ]
    t.devices(headers, [row])
    t.results(row.toString())
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
    t.setMetric("vm_timesync", json.Tools.SyncTimeWithHost)
    t.setMetric("vm_iops_limit", json.Hardware.Device*.StorageIOAllocation.Limit.toString())
    def floppyDevice
    def videoCard
    def disks = []
    json.Hardware.Device.each { node -> 
        // println node
        if (node['Key'] == 8000) {  // Label:Floppy drive
            floppyDevice = node
        }
        if (node['Key'] == 500) {   // Label:Video card
            videoCard = node
        }
        if (node['Key'] == 2000) {  // Label:Hard disk
            disks << node
        }

    }
    t.setMetric("floppyStartConnected", floppyDevice.Connectable?.StartConnected)
    t.setMetric("floppyConnected", floppyDevice.Connectable?.Connected)
// println disks
    t.setMetric("videoRamKB", videoCard.VideoRamSizeInKB)
    def headers = ['dev', 'usage', 'datastore', 'thin', 'writeThrough', 'level', 'shares']
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
        t.newMetric("disk.${label}.size", "[${label}] Size", row[1])
        t.newMetric("disk.${label}.datastore", "[${label}] Datastore", datastore)
        t.newMetric("disk.${label}.thin", "[${label}] ThinProvisioned", row[3])
        t.newMetric("disk.${label}.writeThrough", "[${label}] WriteThrough", row[4])
        t.newMetric("disk.${label}.level", "[${label}] level", row[5])
        t.newMetric("disk.${label}.shares", "[${label}] shares", row[6])
    }
    t.devices(headers, csv)

    def res = [:]
    json.ExtraConfig.each { extraConfig ->
        (extraConfig.Key =~ /^vmware.tools.(.+?)$/).each { m0, m1 ->
            res[m1] = extraConfig.Value
        }
    }
    res['config'] = json.DatastoreUrl*.Name.toString()
    t.results(res)
}

@Parser("guest")
void Guest(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    def headers = ["Network", "Address", "Mac", "Connected", "Dns", 
                   "Dhcp", "AutoConfig"]
    json.Net.each { net ->
        def label = net.Network
        def row = [
            label,
            net.IpAddress,
            net.MacAddress,
            net.Connected,
            net.DnsConfig,
            net.IpConfig?.Dhcp,
            net.IpConfig?.AutoConfigurationEnabled,
        ]
        csv << row
        t.newMetric("net.${label}.Connected", "[${label}] Connected", row[3])
        // t.newMetric("net.${label}.Dns", "[${label}] Dns", row[4])
        // t.newMetric("net.${label}.Dhcp", "[${label}] Dhcp", row[5])
    }
    t.devices(headers, csv)
    t.setMetric("toolsStatus", json.ToolsStatus)
    t.results(json.IpAddress)
}

@Parser("guestHeartbeatStatus")
void GuestHeartbeatStatus(TestUtil t) {
    t.results(t.readAll())
}

