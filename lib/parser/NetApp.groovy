package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

class CSVInfo {
    def headers = []
    def csv     = []
    def rows    = []
}

def parse_csv(String lines) {
    def csv_info = new CSVInfo()
    def header_index = [:]
    def rownum = 0
    def header_number = 0
    lines.eachLine {
        rownum ++
        String[] columns = it.split(/<\|>/)
        if (rownum == 3 && columns.size() > 1) {
            columns.each { header ->
                header_index[header] = header_number
                header_number ++
            }
            csv_info.headers = header_index.keySet() as ArrayList
        } else if (rownum > 3 && header_number == columns.size()) {
            def unique_columns = []
            def row = [:]
            header_index.each { header, cols ->
                row[header] = columns[cols] 
                unique_columns << columns[cols]
            }
            csv_info.rows << row
            csv_info.csv << unique_columns
        }
    }
    return csv_info
}

@Parser("version")
void version(TestUtil t) {
    t.readLine {
        (it =~/^(.+?):\s*(.+?)$/).each { m0, os, release ->
            t.results(os)
            t.setMetric("release_date", release)
        }
    }
}

@Parser("ntp")
void ntp(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    def ntp = []
    csv_info.rows.each { row ->
        row.each { name, value ->
            (name=~/Address/).each {
                ntp << value
            }
        }
    }
    t.results(ntp.toString())
}

@Parser("snmp")
void snmp(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    csv_info.rows?.get(0).each { metric, value ->
        t.setMetric("snmp.${metric}", value)
    }
}

@Parser("vserver")
void vserver(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
}

@Parser("df")
void df(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
}

@Parser("subsystem_health")
void subsystem_health(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    csv_info.rows.each { row ->
        def subsystem = row.Subsystem ?: 'N/A'
        def health = row.Health ?: 'N/A'
        t.setMetric("status.${subsystem}", health)
    }
}

@Parser("storage_failover")
void storage_failover(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    def csv = []
    csv_info.rows?.get(0).each { metric, value ->
        csv << [metric, value]
    }
    t.devices(["name", "value"], csv)
}

@Parser("license")
void license(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    network_interface(t)
}

@Parser("processor")
void processor(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    // println csv_info.rows
    csv_info.rows?.get(0).each { metric, value ->
        // println "os.${metric}  : ${value}"
        t.setMetric("os.${metric}", value)
    }
    t.devices(csv_info.headers, csv_info.csv)
}

@Parser("memory")
void memory(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
}

@Parser("aggregate_status")
void aggregate_status(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    def infos = [:].withDefault{[]}
    csv_info.rows.each { row ->
        infos[row['Size']] << row['Aggregate']
    }
    def id = 1
    infos.each { size, aggregate ->
        t.newMetric("raid.${id}.name", "RAID[${id}] name", aggregate)
        t.newMetric("raid.${id}.size", "RAID[${id}] size", size)
        id ++
    }
}

@Parser("volume")
void volume(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    def infos = [:].withDefault{0}
    csv_info.rows.each { row ->
        infos[row['Volume Size']] += 1
    }
    t.results(infos.toString())
}

@Parser("network_interface")
void network_interface(TestUtil t) {
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
    csv_info.rows.each { row ->
        def ip = row."Network Address"
        def port = row."Current Port" ?: 'N/A'
        if (ip) {
            t.portList(ip, port, true)
        }
    }
}

@Parser("sysconfig")
void sysconfig(TestUtil t) {
    // println t.readAll()
    def infos = [:].withDefault{[:].withDefault{0}}
    def csv = []
    t.readLine {
        (it =~ /^\s*(\w.+?):\s(.+?)$/).each { m0, item, value->
            (item=~/^(Node|System ID|System Serial Number|Processors|Processor type|Memory Size|NVMEM Size)$/).each {
                infos["hw.${item}"] = value.trim()
            }
        }
        csv << [it]
    }
    t.results(infos)
    t.devices(['message'], csv)
}

@Parser("sysconfig_raid")
void sysconfig_raid(TestUtil t) {
    def infos = [:]
    def drive_nodes = [:].withDefault{[:].withDefault{0}}
    def raid_groups = [:].withDefault{0}
    def node       = 'unkown'
    def raid_group = 'unkown'
    def csv = []
    t.readLine {
        (it =~ /^Node: (.+)$/).each {m0, m1 ->
            node = m1
        }
        (it =~ /^\s+RAID group (.+?)\s/).each {m0, m1 ->
            raid_group = m1
        }
        (it =~ /^\s+(dparity|parity|data)\s.+\s(\d+)\/(\d+)\s*$/).each {m0, m1, m2, m3 ->
            drive_nodes[m1]["${m2}MB"] += 1
            raid_groups["${node}.${raid_group}"] += 1
        }
        csv << [it]
    }
    drive_nodes.each { drive_node, info ->
        infos["drive.${drive_node}"] = "${info}"
    }
    infos['sysconfig_raid'] = "${raid_groups.size()} RAID groups"
    t.results(infos)
    t.devices(['message'], csv)
}
