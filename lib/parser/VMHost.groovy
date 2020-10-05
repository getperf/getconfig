package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("summary.json")
void Summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("Version", json.Config.Product.Version)
    t.setMetric("Build", json.Config.Product.Build)
    t.setMetric("PowerState", json.Runtime.PowerState)
    t.setMetric("Manufacturer", json.Hardware.Vendor)
    t.setMetric("NumCpuPkgs", json.Hardware?.NumCpuPkgs)
    t.setMetric("NumCpuCores", json.Hardware?.NumCpuCores)
    t.setMetric("CpuTotalMhz", json.Hardware?.CpuMhz)
    t.setMetric("CputTotal", json.Hardware.findAll { it =~ /NumCpu/ }. toString())
    t.setMetric("ProcessorType", json.Hardware?.CpuModel)

    def os = "ESXi ${json.Config.Product.Version} Build ${json.Config.Product.Build}"
    t.newMetric("summary.version", "ESXi Version", os)
    def arch = "x86_64"
    if (!(json.Hardware?.CpuModel =~ /Intel/)) {
        arch = json.Hardware?.CpuModel
    }
    t.newMetric("summary.arch", "ESXi Architecture", arch)

    def memTotal = (json.Hardware?.MemorySize ?: 0) / (1024.0 ** 3)
    t.setMetric("MemoryTotalGB", memTotal)
}

@Parser("config.json")
void Config(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    def id = 0
    json.Network?.Vnic*.Spec.each {
        t.newMetric("NIC.ip.${id}", "[${id}] IP", it.Ip.IpAddress)
        t.newMetric("NIC.Dhcp.${id}", "[${id}] Dhcp", it.Ip.Dhcp)
        t.newMetric("NIC.IPv6Auto.${id}", "[${id}] IPv6 Auto", it.Ip.IpV6Config.AutoConfigurationEnabled)
        t.newMetric("NIC.Mac.${id}", "[${id}] Mac", it.Mac)
        t.newMetric("NIC.Mtu.${id}", "[${id}] Mtu", it.Mtu)
        t.newMetric("NIC.NetStack.${id}", "[${id}] NetStack", it.NetStackInstanceKey)
        t.newMetric("NIC.Portgroup.${id}", "[${id}] Portgroup", it.Portgroup)

        t.portList(it.Ip.IpAddress, "NIC${id}")
        id ++
    }
    t.setMetric("HyperthreadingActive", json?.HyperThread?.Active)
    t.setMetric("TimeZone", json?.DateTimeInfo?.TimeZone?.Description)
    t.setMetric("Parent", json?.VsanHostConfig?.ClusterInfo?.toString())

    id = 0
    json.FileSystemVolume?.MountInfo*.Volume?.Extent*.DiskName.each {
        (it =~/^(.+?)__+(.+?)__+(.+)/).each { m0, m1, m2, m3 ->
            t.newMetric("Diak.model.${id}", "[${id}] Model", m2)
            id ++
        }
    }
}

@Parser("configManager.json")
void ConfigManager(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("FirewallDefaultPolicy", json.FirewallSystem.toString())
}

@Parser("capability.json")
void Capability(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("VMSwapfilePolicy", json.findAll { it =~ /Swap/ }.toString())
}

