package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

String getLabel(metric, values) {
    final def zabbix_labels = [
        'status' : [
            '0' : 'Monitored',
            '1' : 'Unmonitored',
        ],
        'available' : [
            '0' : 'Unknown',
            '1' : 'Available',
            '2' : 'Unavailable',
        ],
        'trigger.status' : [
            '0' : 'Enabled',
            '1' : 'Disabled',
        ],
        'state' : [
            '0' : 'Normal',
            '1' : 'Unknown',
        ],
        'priority' : [
            '0' : 'Not classified',
            '1' : 'Information',
            '2' : 'Warning',
            '3' : 'Average',
            '4' : 'High',
            '5' : 'Disaster',
        ]
    ]

    String label = 'N/A'
    if (zabbix_labels[metric]) {
        def id = values?.get(metric)
        if (id && zabbix_labels[metric][id]) {
            label = zabbix_labels[metric][id]
        }
    }
    return label
}

@Parser("hosts")
void Hosts(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json.macros ).toPrettyString()
    t.results(json.hostid ?: 'N/A')
    t.setMetric("hostGroup", json.groups*.name ?: 'N/A')
    t.setMetric("template", json.parentTemplates*.name ?: 'N/A')
    t.setMetric("status", getLabel('status', json))
    t.setMetric("available", getLabel('available', json))
    def csvGroup = []
    json.groups*.name.each { String group ->
        json.parentTemplates*.name.each { String template ->
            csvGroup << [group, template]
        }
    }
    t.devices(['group', 'template'], csvGroup, 'hostGroup')
    def interfaces = []
    json.interfaces.each { 
        String address = (it?.useip == '1') ? it?.ip : it?.dns
        interfaces << address + ":${it?.port}"
    }
    t.setMetric("interface", interfaces ?: 'N/A')

    def macros = []
    t.setMetricFile('macros')
    t.results((json.macros) ? 'Enable' : 'N/A')
    def csvMacros = []
    json.macros.each { 
        t.newMetric("macros.${it.macro}", it.macro, it.value)
        csvMacros << [it.macro, it.value]
    }
    t.devices(['macro', 'value'], csvMacros, 'macros')
}

@Parser("logItems")
void LogItems(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    // println new JsonBuilder( json ).toPrettyString()
    def csv = []
    String status = 'Nornal'
    json.each { logItem ->
        // println logItem
        def name   = logItem?.name
        def state= getLabel('state', logItem)
        if (state != 'Normal') {
            status = state
        }
        csv << [name, state, logItem?.error, logItem?.key_]
    }
    t.devices(['name', 'state', 'error', 'key'], csv)
    t.results(status)
}

@Parser("triggers")
void Triggers(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def headers = ['priority', 'description', 'expression', 'flags', 
                   'state', 'status']
    def csv   = []
    json.each { trigger->
        def columns = []
        def values = [:]
        String description, status
        headers.each { item ->
            def value = trigger.get(item) ?: 'N/A'
            if (item in ['priority', 'state', 'status']) {
                value = getLabel(item, trigger)
            } else if (item == 'description') {
                value = value.replaceAll(/${t.serverName}/,"{HOST}")
                description = value
            }
            columns.add(value)
            values[item] = value
        }
        t.newMetric(description, "triggers.${description}", values['status'])
        csv << columns
    }
    t.devices(headers, csv)
    // println csv
    // t.results(json.Guest?.HostName ?: 'N/A')
    // t.setMetric("numCpu", json.Config?.NumCpu ?: 'N/A')
    // t.setMetric("powerState", json.Runtime?.PowerState ?: 'N/A')
    // t.setMetric("memoryMB", json.Config?.MemorySizeMB ?: 'N/A')
    // t.setMetric("hostName", json.Runtime?.Host?.Name ?: 'N/A')
}
