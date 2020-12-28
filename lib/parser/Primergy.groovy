package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("overview")
void overview(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = json.Oem?.ts_fujitsu?.AutoDiscoveryDescription?.ChassisInformation
    t.setMetric("product_name", info?.Model ?: 'N/A')
    t.setMetric("serial_num", info?.SerialNumber ?: 'N/A')
}

@Parser("firmware")
void firmware(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("product_id", json?.SDRRId ?: 'N/A')
    t.setMetric("bios", json?.SystemBIOS ?: 'N/A')
    t.setMetric("bmc", json?.BMCBaseFirmware ?: 'N/A')
    t.setMetric("bmc_build", json?.BMCFirmwareBuildDate ?: 'N/A')
}

@Parser("network")
void network(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def addresses = json.IPv4Addresses
    def csv = []
    addresses.each { 
        csv << [it?.Address, it?.SubnetMask, it?.AddressOrigin, it?.Gateway]
        if (it?.Address) {
            t.portList(it?.Address, 'iRMC', true)
        }

    }    
    t.devices(['Adress', 'Subnet', 'Origin', 'Gateway'], csv)
    t.results(addresses*.Address.toString())
}

@Parser("nic")
void nic(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def ports = json.Ports
    def csv = []
    ports.each { 
        csv << [it?.BootOption, it?.FirmwareVersion, it?.SpeedMbps, 
                it?.AdapterName, it?.PortStatus, it?.MacAddress]
    }    
    t.devices(['Boot', 'Firmware', 'Speed', 'Adapter', 'Status', 'Mac'], csv)
}

@Parser("ntp0")
void ntp0(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.results(json?.NtpServerName ?: 'N/A')
}

@Parser("ntp1")
void ntp1(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.results(json?.NtpServerName ?: 'N/A')
}

def size_info(arr = [:]) {
    if (arr['#text'] && arr['@Unit']) {
        return "${arr['#text']}${arr['@Unit']}"
    } else {
        return 'Unkown'
    }
}

void physical_disks(TestUtil t, disks) {
    def labels = [:].withDefault{0}
    def csv = []
    def headers = ['Slot', 'PDStatus', 'Interface', 'Type', 'Vendor',
         'Product', 'Size']
    disks.each { disk ->
        def row = []
        def label = []
        headers.each { header ->
            def value = disk.get(header)
            if (header == 'Size') {
                value = size_info(value)
            }
            if (value == null) {
                value = 'N/A'
            }
            if (header == 'Type' || header == 'Size') {
                label << value
            }
            row << value
        }
        csv << row
        labels[label.toString()] ++
    }
    t.devices(headers, csv, "disk_drive")
    t.setMetric("disk_drive", labels.toString())
}

void logical_disks(TestUtil t, disks) {
    def labels = [:].withDefault{0}
    def csv = []
    def headers = ['RaidLevel', 'WriteMode', 'ReadMode', 
        'CacheMode', 'DiskCacheMode', 'Stripe', 'InitMode', 
        'LDStatus', 'Name', 'Size']
    disks.each { disk ->
        def row = []
        def label = []
        headers.each { header ->
            def value = disk.get(header)
            if (header == 'Size' || header == 'Stripe') {
                value = size_info(value)
            }
            if (value == null) {
                value = 'N/A'
            }
            if (header == 'RaidLevel') {
                label << "RAID${value}"
            }
            if (header == 'Size') {
                label << value
            }
            row << value
        }
        csv << row
        labels[label.toString()] ++
    }
    t.devices(headers, csv, "disk")
    t.setMetric("disk", labels.toString())
}

@Parser("disk")
void disk(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def infos = json.get("Server")?.
        get("HWConfigurationIrmc")?.
        get("Adapters")?.
        get("RAIDAdapter")?.
        get(0)
    physical_disks(t, infos?.get('PhysicalDisks')?.get('PhysicalDisk'))
    logical_disks(t, infos?.get('LogicalDrives')?.get('LogicalDrive'))
}

@Parser("snmp")
void snmp(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def results = [:]
    def infos = json.get("Server")?.
        get("SystemConfig")?.
        get("IrmcConfig")?.
        get("NetworkServices")

    infos?.get('Snmp').with {
        def trap_dests = []
        it['TrapDestinations']['TrapDestination'].each {
            if (it['Name'])
                trap_dests << "${it['Name']} ${it['Protocol']}"
        }
        t.setMetric("snmp.trap", it?.Enabled)
        t.setMetric("snmp.community", it?.CommunityName)
        t.setMetric("snmp.dest", (trap_dests.size() == 0) ? 
            "NoDestination" : trap_dests.toString())
        it.each { item,value ->
            if (item == 'ServicePort') {
                value = "'${value}'"
            }
            t.setMetric("snmp.port", value)
        }
    }
}
