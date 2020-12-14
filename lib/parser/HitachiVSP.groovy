package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("storage")
void storage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json ).toPrettyString()

    ['storageDeviceId', 'model', 'serialNumber', 'gumVersion',
    'ctl1Ip', 'ctl2Ip', 'dkcMicroVersion'].each { String metric ->
        if (metric == 'serialNumber') {
            t.setMetric(metric, "${json[metric]}")
        } else {
            t.setMetric(metric, json[metric] ?: 'N/A')
        }
    }
    if (json?.ctl1Ip) {
        t.portList(json?.ctl1Ip, 'VSPCtl1', true)
    }
    if (json?.ctl2Ip) {
        t.portList(json?.ctl2Ip, 'VSPCtl2', true)
    }
}

@Parser("host-groups")
void hostGroups(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ['hostGroupId', 'portId', 'hostGroupNumber', 
        'hostGroupName', 'hostMode']
    def csv = []
    json.data.each { hostGroup ->
        def row = []
        headers.each { header ->
            row << hostGroup?."$header" ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results("${csv.size()} host groups")
}

@Parser("ports")
void ports(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ["portId", "portType", "portAttributes", 
        "portSpeed", "loopId", "fabricMode", "portConnection", 
        "lunSecuritySetting", "wwn"]
    def csv = []
    json.data.each { port ->
        def row = []
        headers.each { header ->
            row << port?."$header" ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results("${csv.size()} ports")
}

@Parser("parity-groups")
void parityGroups(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ["parityGroupId", "numOfLdevs", 
        "usedCapacityRate", "availableVolumeCapacity", "raidLevel", 
        "raidType", "clprId", "driveType", "driveTypeName", 
        "totalCapacity", "isAcceleratedCompressionEnabled"]
    def csv = []
    def drives = [:].withDefault{0}
    json.data.each { raidGroup ->
        def row = []
        def drive = []
        headers.each { header ->
            def value = raidGroup?."$header" ?: 'N/A'
            row << value
            (header =~/(raidType|driveTypeName|totalCapacity)/).each {
                drive << value
            }
        }
        csv << row
        drives[drive] ++
    }
    t.devices(headers, csv)
    t.results(drives.toString())
}

@Parser("ldevs")
void ldevs(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ["ldevId", "clprId", "emulationType", 
        "byteFormatCapacity", "blockCapacity", "composingPoolId", 
        "attributes", "raidLevel", "raidType", "numOfParityGroups", 
        "parityGroupIds", "driveType", "driveByteFormatCapacity", 
        "driveBlockCapacity", "status", "mpBladeId", "ssid", 
        "resourceGroupId", "isAluaEnabled"]

    def csv = []
    json.data.each { ldev ->
        def row = []
        if (ldev?."emulationType"=~/NOT DEFINED/) {
            return
        }
        headers.each { header ->
            row << ldev?."$header" ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results("${csv.size()} logical devices")
}

@Parser("users")
void users(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json ).toPrettyString()

    def headers = ["userObjectId","userId","authentication",
        "userGroupNames","isBuiltIn","isAccountStatus"]

    def csv = []
    json.data.each { user ->
        def row = []
        headers.each { header ->
            row << user?."$header" ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results("${csv.size()} users")
}

@Parser("snmp")
void snmp(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    ["isSNMPAgentEnabled", "snmpVersion"].each { String metric ->
        t.setMetric(metric, json[metric] ?: 'N/A')
    }

    ["contact", "location"].each { String metric ->
        t.setMetric(metric, json?.systemGroupInformation[metric] ?: 'N/A')
    }
    def res = [:].withDefault{[]}
    json.sendingTrapSetting?.snmpv1v2cSettings.each { trap ->
        res["snmpTrapCommunity"] << trap.community ?: 'N/A'
        res["snmpTrapTo"] << trap.sendTrapTo ?: 'N/A'
    }
    json.requestAuthenticationSetting?.snmpv1v2cSettings.each { trap ->
        res["snmpCommunity"] << trap.community ?: 'N/A'
        res["snmpRequestsPermitted"] << trap.requestsPermitted ?: 'N/A'
    }
    t.results(res)
}

@Parser("ambient")
void ambient(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.setMetric("powerConsumption", json.system?.powerConsumption ?: 'N/A')
    def csv = []
    def res = [:].withDefault{0}
    def headers = ["location","status","temperature"]
    json.ctls.each { ambient ->
        res[ambient?.status]++
        def row = []
        headers.each { header ->
            row << ambient?."$header" ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv, "status")
    t.setMetric("status", res.toString())
}
