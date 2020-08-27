package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("summary.json")
void Summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("vm.name", json.Guest.HostName)
    t.setMetric("vm.NumCpu", json.Config.NumCpu)
    t.setMetric("vm.PowerState", json.Runtime.PowerState)
    t.setMetric("vm.MemoryGB", json.Config.MemorySizeMB)
}

@Parser("resourceConfig.json")
void ResourceConfig(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    def limits = [
        "cpu": json.CpuAllocation.Limit,
        "mem": json.MemoryAllocation.Limit,
    ]
    t.setMetric("vm_conf.limit", limits.toString())

    def share_levels = [
        "cpu": json.CpuAllocation.Shares.Level,
        "mem": json.MemoryAllocation.Shares.Level,
    ]
    t.setMetric("vm_conf.shares_level", share_levels.toString())
}


@Parser("config.json")
void Config(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("vm_conf.cpu_affinity", 
                [
                    "cpu": json.CpuAffinity,
                    "mem": json.MemoryAffinity,
                ].toString())

    t.setMetric("vmext.CpuHotAddEnabled", 
                [
                    "cpu": json.CpuHotAddEnabled,
                    "mem": json.MemoryHotAddEnabled,
                ].toString())

    t.setMetric("vmext.MemoryReservationLockedToMax",
                json.MemoryReservationLockedToMax)

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
    t.setMetric("vm_floppy", floppyDevice.Connectable)
    t.setMetric("vm_video_ram", videoCard.VideoRamSizeInKB)

    disks.each { disk ->
        def label = disk.DeviceInfo.Label
        if (!label) {
            return
        }
        label = CommonUtil.toCamelCase(label)
        t.newMetric("vm_storage.${label}.summary", "[${label}] 容量", disk.DeviceInfo.Summary)
        t.newMetric("vm_storage.${label}.uuid",    "[${label}] UUID", disk.Backing.Uuid)
        t.newMetric("vm_storage.${label}.ThinProvisioned", "", disk.Backing.ThinProvisioned)
        t.newMetric("vm_storage.${label}.WriteThrough", "",    disk.Backing.WriteThrough)
        t.newMetric("vm_storage.${label}.Level",  "",          disk.Shares.Level)
        t.newMetric("vm_storage.${label}.Shares", "",          disk.Shares.Shares)
    }
    def vmwareTools = [:]
    json.ExtraConfig.each { extraConfig ->
        (extraConfig.Key =~ /^vmware.tools.(.+?)$/).each { m0, m1 ->
            vmwareTools[m1] = extraConfig.Value
        }
    }
    t.setMetric("vmwaretool.version", json.DatastoreUrl*.Name.toString())
}

@Parser("guest.json")
void Guest(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric("vmnet", json.Net*.Network.toString())
}
