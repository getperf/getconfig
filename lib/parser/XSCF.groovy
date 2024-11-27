package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.CommonUtil

def trim(str){
    str.replaceAll(/\A[\s:]+/,"").replaceAll(/[\s]+\z/,"")
}

def parse_module(String module, String separator) {
    def module_type = module
    def module_suffix = ''
    (module =~ /^(.+)${separator}(.+)$/).each { m0, _type, _suffix ->
        module_type = _type
        module_suffix = _suffix
    }
    return [module_type, module_suffix]
}

@Parser("hostname")
void hostname(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("fwversion")
void fwversion(TestUtil t) {
    def module_status = [:]
    def csvs = []
    def xscf = 'unkown'

    t.readLine {
        ( it =~ /(.+):(.+)/).each {m0, module, version->
            csvs << [module, version]
            module = trim(module)
            version = trim(version)
            version = "'${version}'"
            // Trim "XCP0 (Reserve)"
            (module =~ /^(.+) \(/).each { n0, n1 ->
                module = n1
            }
            (module =~ /^\#/).each { n0 ->
                module = ''
            }
            if (module) {
                module_status[module] = version
                if (module == 'XSCF') {
                    xscf = version
                }
            }
        }
    }
    module_status.each { module, status ->
        t.newMetric("fwversion.${module}", "[${module}]", status)
    }
    t.devices(['node', 'version'], csvs)
    t.results(xscf)
}

@Parser("hardconf")
void hardconf(TestUtil t) {
    def module_status = []
    def csvs = []
    def moudle_infos = [:].withDefault{[:].withDefault{[]}}

    t.readLine {
        ( it =~ /^(\w.+?);\s*$/).each {m0, m1 ->
            t.setMetric("hardconf.system", m1)
        }
        ( it =~ /^\s+\+\sSerial:(.+?);\s*/).each {m0, m1 ->
            t.setMetric("hardconf.serial", m1)
        }
        ( it =~ /(\w.+?) Status:(.+)/).each {m0, module, column_str->
            module = trim(module)
            def module_info = parse_module(module, "#")
            def columns = column_str.split(/;/)
            def stat = columns[0]
            moudle_infos[module_info[0]][stat] << module_info[1]
            if (stat != 'Normal' && stat != 'Running' &&
                stat != 'ON') {
                module_status << "$module:$stat" 
            }
            csvs << [module, stat, column_str]
        }
    }
    t.devices(['node', 'status', 'description'], csvs)
    moudle_infos.each { module_type, module_statuses -> 
        def result_lines = []
        module_statuses.each { status, modules ->
            if (modules.size() == 1) {
                result_lines << status
            } else if (modules.size() > 1) {
                result_lines << "$status:$modules"
            }
        }
        t.newMetric("hardconf.${module_type}", "[${module_type}]",
                    result_lines)
    }
    def res = (module_status.size() > 0) ? "${module_status}" : 'Normal'
    t.results(res)
}

@Parser("cpu_activate")
void cpu_activate(TestUtil t) {
    def cpu_status = [:].withDefault{0}
    def csvs = []
    t.readLine {
        ( it =~ /PROC Permits\s+(.+?):\s+(\d+)/).each {m0, module, core->
            def module_info = parse_module(module, ' for ')
            cpu_status[module_info[0]] += core.toInteger()
            t.newMetric("cpu_activate.${module}", "[${module}]", core)
            csvs << [module, core]
        }
    }
    t.devices(['module', 'core'], csvs)

    def res = "${cpu_status['assigned']} / ${cpu_status['installed']} Core"
    t.results(res)
}

@Parser("network")
void network(TestUtil t) {
    def sequence = 0
    def infos = [:].withDefault{[:]}
    def ip_addresses = [:]
    def res = [:]
    t.readLine {
        ( it =~ /Link/).each {
            sequence ++
        }
        ( it =~ /inet addr:(.+?)\s/).each {m0, value->
            infos[sequence]['ip'] = value
            ip_addresses[value] = 1
        }
        ( it =~ /HWaddr (.+?)$/).each {m0, value->
            infos[sequence]['mac'] = value
        }
        ( it =~ /Mask:(.+?)$/).each {m0, value->
            infos[sequence]['mask'] = value
        }
    }
    def csv = []
    infos.each { device, items ->
        def columns = [device]
        ['ip', 'mac', 'mask'].each {
            columns.add(items[it] ?: 'NaN')
        }
        csv << columns
        def ip_address = infos[device]['ip']
        if (ip_address && ip_address != '127.0.0.1') {
            t.portList(ip_address, 'XSCF', true)
            t.newMetric("network.ip.${device}", "[${device}] IP", ip_address)
            t.newMetric("network.mask.${device}", "[${device}] ネットマスク",
                        infos[device]['mask'])
            t.newMetric("network.mac.${device}", "[${device}] MAC", 
                        infos[device]['mac'])
        }
    }
    t.devices(['device', 'ip', 'mac', 'subnet'], csv)
    t.results(ip_addresses.toString())
}

@Parser("snmp")
void snmp(TestUtil t) {
    def infos = [:]
    t.readLine {
        // line format 
        ( it =~ /(?i)Agent Status:\s+(.+?)$/).each {m0, value->
            infos['snmp_agent_status'] = value
        }
        ( it =~ /(?i)Agent Port:\s+(.+?)$/).each {m0, value->
            infos['snmp_agent_port'] = "'${value}'"
        }
        ( it =~ /(?i)Trap Hosts:\s+(.+?)$/).each {m0, value->
            infos['snmp_trap_host'] = value
        }
        ( it =~ /SNMP (.+):\s+(.+?)$/).each {m0, m1, value->
            infos['snmp_version'] = "'${value}'"
        }
        // table format
        ( it =~ /^(\w+?)\s+(\d+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)$/).each {m0, m1, m2, m3, m4, m5, m6, m7->
            if (m1.size() > 0) {
                infos['snmp_trap_host'] = m1
            }
            if (m2.size() > 0) {
                infos['snmp_trap_port'] = "'${m2}'"
            }
            if (m3.size() > 0) {
                infos['snmp_version'] = "'${m3}'"
            }
            if (m4.size() > 0) {
                infos['snmp_community'] = m4
            }
        }
    }
    def csv = []
    def columns = []
    ['snmp_agent_status', 'snmp_version', 'snmp_agent_port', 'snmp_trap_host', 'snmp_trap_port', 'snmp_community'].each { metric ->
        def value = infos[metric] ?: 'NaN'
        columns.add(value)
        t.newMetric("snmp.${metric}", metric, value)
    }

    csv << columns
    def headers = ['status', 'version', 'agent_port', 'host', 'host_port', 'community']
    t.devices(headers, csv)
    t.results(infos['snmp_agent_status'] ?: 'Not found')

}
