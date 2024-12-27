package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.CommonUtil

double toGiB(bytes) {
    return bytes / (1024.0*1024.0*1024.0)
}

@Parser("arrays")
void arrays(TestUtil t) {
    def arrays = new JsonSlurper().parseText(t.readAll())
    println new JsonBuilder( arrays ).toPrettyString()
    arrays.each { array ->
        t.setMetric("arrays", array.name ?: "N/A")
        t.setMetric("brand", array.brand ?: "N/A")
        t.setMetric("model", array.model ?: "N/A")
        t.setMetric("model_sub_type", array.model_sub_type ?: "N/A")
        t.setMetric("extended_model", array.extended_model ?: "N/A")
        t.setMetric("serial", array.serial ?: "N/A")
        t.setMetric("version", array.version ?: "N/A")
        t.setMetric("status", array.status ?: "N/A")
        t.setMetric("pool_name", array.pool_name ?: "N/A")
        t.setMetric("status", array.status ?: "N/A")
        t.setMetric("pool_name", array.pool_name ?: "N/A")
        t.setMetric("usable_capacity_bytes", toGiB(array.usable_capacity_bytes) ?: "N/A")
        t.setMetric("available_bytes", toGiB(array.available_bytes) ?: "N/A")
        t.setMetric("usage", toGiB(array.usage) ?: "N/A")
    }
    // println new JsonBuilder( t.portListGroup ).toPrettyString()
    // ToDo: IPアドレスの抽出
}

@Parser("disks")
void disks(TestUtil t) {
    def csv = []
    def disks = new JsonSlurper().parseText(t.readAll())
    def infos = [:].withDefault{0}

    disks.each { disk ->
        def type = disk.type ?: 'unkowon'
        def disk_size = String.format("%.1f", toGiB(disk.size ?: 0))
        // println "${type}:${disk_size}GB"
        infos["${type}:${disk_size}GB"] ++
        csv << [
            disk.serial ?: "N/A",
            disk.path ?: "N/A",
            disk.shelf_serial ?: "N/A",
            disk.shelf_location ?: "N/A",
            disk.slot ?: "N/A",
            disk.bank ?: "N/A",
            disk.model ?: "N/A",
            disk.vendor ?: "N/A",
            disk.firmware_version ?: "N/A",
            disk_size,
        ] 
    }
    def headers = ['serial', 'path', 'shelf_serial', 'shelf_location', 'slot', 'bank', 'model', 'vendor', 'firmware_version', 'disk_size']
    t.devices(headers, csv);
    t.results("${infos}")
}

