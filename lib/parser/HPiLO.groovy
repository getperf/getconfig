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
        t.setMetric("overview.${metric}", json[metric])
     }
}

@Parser("License")
void License(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    def license = json.License ?: 'N/A'
    def key = json.ConfirmationRequest?.EON?.LicenseKey ?: 'N/A'
    t.setMetric("License", license)
    t.setMetric("License.Key", key)
    t.devices(['license', 'key'], [[license, key]])
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
    t.results(res.toString())
}

@Parser("mem_info")
void mem_info(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def memory_size_gb = 0
    ['mem_total_mem_size', 'mem_op_speed', 'mem_condition'].each {
        String metric ->
        def value = json."$metric" ?: 'N/A'
        if (metric == 'mem_total_mem_size') {
            memory_size_gb = value / 1024
        }
        t.setMetric("mem_info.${metric}", value)
    }
    t.results(memory_size_gb.toString())
}

@Parser("network")
void network(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

@Parser("health_phy_drives")
void storage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

@Parser("health_drives")
void drive(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

@Parser("snmp")
void snmp(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

@Parser("power_regulator")
void power_regulator(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

@Parser("power_summary")
void power_summary(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

}

