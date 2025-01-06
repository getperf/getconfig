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
    // println new JsonBuilder( arrays ).toPrettyString()
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
}

@Parser("disks")
void disks(TestUtil t) {
    def result = 'NG'
    def disks = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( disks ).toPrettyString()
    def infos = [:].withDefault{0}
    disks.each { disk ->
        def type = disk.type ?: 'unkowon'
        def disk_size = String.format("%.1f", toGiB(disk.size ?: 0))
        // println "${type}:${disk_size}GB"
        infos["${type}:${disk_size}GB"] ++ 
    }
    // print infos
    t.results("${infos}")
}

@Parser("networks")
void networks(TestUtil t) {
    def networks = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( networks ).toPrettyString()
    def ipAddresses = [:]
    networks.each { network ->
        def device = network.name ?: 'unkown'
        // print "device:${device}\n"
        network?.ip_list.each { ip ->
            def address = ip.ip
            if (address) {
                ipAddresses[address] = device
            }
        }
    }
    ipAddresses.each { ipAddress, device -> 
        t.portList(ipAddress, device)
    }
    t.results("${ipAddresses}")
}

@Parser("netconfig")
void netconfig(TestUtil t) {
    def netconfig = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( netconfig ).toPrettyString()
    def ipAddresses = [:]
    netconfig.each { network ->
        network.each { item, value ->
            if (item =~/_ip$/) {
                ipAddresses[value] = item
            } 
        }
        network.array_list.each { array_list ->
            array_list.each { item, value ->
                if (item =~/_ip$/) {
                    ipAddresses[value] = item
                } 
            }
        }
    }
    ipAddresses.each { ipAddress, device -> 
        t.newMetric("netconfig.${device}", device, ipAddress)
    }
    t.results((ipAddresses)?'OK':'NG')
}
