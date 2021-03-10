package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("summary")
void Summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.results(json.Config.Product.Version)
    t.setMetric("Build", json.Config.Product.Build)
    t.setMetric("PowerState", json.Runtime.PowerState)
    t.setMetric("Manufacturer", json.Hardware.Vendor)
    t.setMetric("NumCpuPkgs", json.Hardware?.NumCpuPkgs)
    t.setMetric("NumCpuCores", json.Hardware?.NumCpuCores)
    t.setMetric("CpuTotalMhz", json.Hardware?.CpuMhz)
    t.setMetric("CputTotal", json.Hardware?.NumCpuThreads)
    t.setMetric("ProcessorType", json.Hardware?.CpuModel)

    def os = "ESXi ${json.Config.Product.Version} Build ${json.Config.Product.Build}"
    t.setMetric("osname", os)
    def arch = "x86_64"
    if (!(json.Hardware?.CpuModel =~ /Intel/)) {
        arch = json.Hardware?.CpuModel
    }
    t.setMetric("arch", arch)

    def memTotal = (json.Hardware?.MemorySize ?: 0) / (1024.0 ** 3)
    t.setMetric("MemoryTotalGB", memTotal)
}

void addNetwork(TestUtil t, json) {
    def id = 0
    def csv = []
    t.setMetricFile('network')
    def headers = ['Ip', 'Dhcp', 'IpV6Auto', 'Mac', 'Mtu', 'NetStack', 'PortGroup']
    json.Network?.Vnic*.Spec.each {
        def row = [
            it.Ip.IpAddress,
            it.Ip.Dhcp,
            it.Ip.IpV6Config?.AutoConfigurationEnabled,
            it.Mac,
            it.Mtu,
            it.NetStackInstanceKey,
            it.Portgroup,
        ]
        csv << row
        t.newMetric("NIC.ip.${id}", "[${id}] IP", row[0])
        t.newMetric("NIC.Dhcp.${id}", "[${id}] Dhcp", row[1])
        t.newMetric("NIC.IPv6Auto.${id}", "[${id}] IPv6 Auto", row[2])
        // t.newMetric("NIC.Mtu.${id}", "[${id}] Mtu", row[4])
        // t.newMetric("NIC.NetStack.${id}", "[${id}] NetStack", row[5])
        t.newMetric("NIC.Portgroup.${id}", "[${id}] Portgroup", row[6])

        t.portList(it.Ip.IpAddress, "NIC${id}")
        id ++
    }
    t.devices(headers, csv)

    t.results(json.Network?.Vnic*.Spec?.Ip?.IpAddress.toString())
    t.setMetric("DefaultGateway", json.Network?.IpRouteConfig?.DefaultGateway)
    t.setMetric("SubnetMask", json.Network?.Vnic*.Spec?.Ip?.SubnetMask)
}

void addDisk(TestUtil t, json) {
    id = 0
    def diskSizes = [:].withDefault{0}
    def csv_disk = []
    json?.StorageDevice?.ScsiLun.each { lun ->
        def block = (double)(lun?.Capacity?.Block ?: 0)
        def size = (double)(lun?.Capacity?.BlockSize ?: 0)
        def diskTotal = (block * size) / (1024.0 ** 3) as Integer
        (lun?.CanonicalName =~/^(.+?)__+(.+?)__+(.+)/).each { m0, m1, m2, m3 ->
            id ++
            diskSizes["${diskTotal}G"] ++
            csv_disk << [m2, diskTotal]
        }
    }
    // json?.StorageDevice?.HostBusAdapter.each { lun ->
    //     def device = lun.Device
    //     def model = lun.Model
    //     if (device) {
    //         diskSizes[device] = 0
    //         csv_disk << [model, 0]
    //     }
    //     // println new JsonBuilder( lun ).toPrettyString()
    //     // return true
    // }
    json.FileSystemVolume?.MountInfo*.Volume.each { vol ->
        // println new JsonBuilder( vol ).toPrettyString()
        def type = vol.Type
        def name = vol.Name
        def capacity = vol.Capacity
        if (type && name && capacity) {
            def device = "${type}:${name}"
            def size = capacity / (1024.0 ** 3) as Integer
            diskSizes[name] = size
            csv_disk << [device, size]
        }
    }
    t.setMetric("Disk", diskSizes.toString())
    t.devices(['Model', 'Size'], csv_disk, "Disk")
}

@Parser("config")
void Config(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    addNetwork(t, json)
    addDisk(t, json)

    t.setMetric("HyperthreadingActive", json?.HyperThread?.Active)
    t.setMetric("TimeZone", json?.DateTimeInfo?.TimeZone?.Description)
    t.setMetric("Parent", json?.VsanHostConfig?.ClusterInfo?.toString())
    t.setMetric("NTP", json?.DateTimeInfo?.NtpConfig?.Server?.toString())

}

// @Parser("configManager")
// void ConfigManager(TestUtil t) {
//     def json = new JsonSlurper().parseText(t.readAll())
//     t.setMetric("FirewallDefaultPolicy", json.FirewallSystem.toString())
// }

@Parser("capability")
void Capability(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json ).toPrettyString()
    json.findAll { 
        // println it        
        // value = 'N/A' if (value == null)
    }
    // t.setMetric("VMSwapfilePolicy", json.findAll { it =~ /Swap/ }.toString())
}

