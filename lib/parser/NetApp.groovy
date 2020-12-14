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

@Parser("subsystem_health")
void subsystem_health(TestUtil t) {
    println t.readAll()
    def csv_info = this.parse_csv(t.readAll())
    t.devices(csv_info.headers, csv_info.csv)
}
