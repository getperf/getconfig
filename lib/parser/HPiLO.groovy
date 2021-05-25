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

    ['product_name','product_id','serial_num','license',
     'ilo_fw_version','isUEFI','system_rom','ip_address',
     'system_health'].each { String metric ->
        t.setMetric(metric, json[metric])
    }
    if (json?.ip_address) {
        t.portList(json?.ip_address, 'HPiLO', true)
    }
}

@Parser("license")
void license(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def license = json.License ?: 'N/A'
    def key = json.ConfirmationRequest?.EON?.LicenseKey ?: 'N/A'
    t.setMetric("license_key", key)
    t.devices(['license', 'key'], [[license, key]])

    t.results(license)
}

@Parser("proc_info")
void proc_info(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def res = [:].withDefault{0}

    def headers = ['proc_socket', 'proc_name', 'proc_num_cores', 'proc_num_threads', 'proc_status', 'proc_num_l1cache', 'proc_num_l2cache', 'proc_num_l3cache']
    def titles = ['proc_name', 'proc_num_cores', 'proc_num_threads']
    def csv = []

    json.processors.each { processor ->
        def row = []
        def cpu_titles = []
        headers.each { header ->
            def value = processor?."$header" ?: 'N/A'
            row << value
            if (header in titles) {
                cpu_titles << value
            } 
        }
        def cpu_title = cpu_titles.join(' / ')
        res[cpu_title] ++
        csv << row
    }
    t.devices(headers, csv)
    t.setMetric("proc_info", res.toString())
}

@Parser("mem_info")
void mem_info(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric("mem_info", (json.mem_total_mem_size ?: 0) / 1024)
    t.setMetric("mem_op_speed", json.mem_op_speed ?: 'N/A')
    t.setMetric("mem_condition", json.mem_condition ?: 'N/A')

    def csv = []
    json.memory.each { memory ->
        if (memory?.mem_size > 0) {
            csv << [memory?.mem_dev_loc, memory?.mem_size, memory?.mem_speed]
        }
    }
    t.devices(['dev_loc', 'size', 'speed'], csv)
}

@Parser("network")
void network(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    json.IPv4Addresses*.with { ipv4 ->
        t.setMetric("net.address", ipv4.Address)
        t.setMetric("net.gateway", ipv4.Gateway)
        t.setMetric("net.origin",  ipv4.AddressOrigin)
        t.setMetric("net.subnet",  ipv4.SubnetMask)
        csv << [ipv4.Address, ipv4.Gateway, ipv4.AddressOrigin, ipv4.SubnetMask]
    }
    t.devices(['address', 'gateway', 'origin', "subnet"], csv)

    json.IPv6Addresses*.with { ipv6 ->
        t.setMetric("ipv6.address", ipv6.Address)
        t.setMetric("ipv6.origin", ipv6.AddressOrigin)
    }
    t.setMetric("net.autoneg", json.AutoNeg)
    t.setMetric("net.mac", json.MACAddress)
    t.results(json.Status.toString())
}

@Parser("health_phy_drives")
void storage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def res = [:].withDefault{0}
    def csv = []
    json.phy_drive_arrays?.with {
        t.results(it?.encr_self_stat.toString())
        it?.physical_drives*.each { drive ->
            csv << [ it*.model, it*.serial_no, it*.fw_version,
                     drive.name, drive.model, drive.serial_no, 
                     drive.fw_version, drive.capacity]
            res[drive.capacity] ++
        }
    }
    t.setMetric("disk.drives", res.toString())
    def headers = ["cont", "cont_sn", "cont_fw", "name", "model", 
                   "serial_no", "fw_version", "capacity"]
    t.devices(headers, csv)
}

@Parser("health_drives")
void drive(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def res = [:].withDefault{0}
    def csv = []
    json.log_drive_arrays?.logical_drives*.each { drive ->
        t.results(drive?.status.toString())
        csv << [ drive.name, drive.physical_drives, drive.status, 
                 drive.flt_tol, drive.capacity]
        res["${drive.flt_tol},${drive.capacity}"] ++
    }
    t.setMetric("disk.raid", res.toString())
    def headers = ["name", "drives", "status", "flt_tol", "capacity"]
    t.devices(headers, csv)
}

@Parser("snmp")
void snmp(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric("snmp", json.SNMPAlertProtocol ?: 'N/A')
    t.setMetric("snmp.community", json.TrapCommunity ?: 'N/A')
    t.setMetric("snmp.dest", json.AlertDestination ?: 'N/A')
}

@Parser("power_regulator")
void power_regulator(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.results(json.prmode ?: 'N/A')
    // println new JsonBuilder( json ).toPrettyString()
}

@Parser("power_summary")
void power_summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric('power_summary', json.max_measured_wattage ?: 'N/A')
    t.setMetric('power_cap_mode', json.power_cap_mode ?: 'N/A')
}

