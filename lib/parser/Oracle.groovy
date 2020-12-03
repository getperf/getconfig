package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("dbattrs")
void dbattrs(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json?.get(0).each { name, value ->
        csv << [name, value]
        info["dbattrs.${name.toLowerCase()}"] = value
    }
    t.devices(['name', 'value'], csv)
    t.results(info)
}

@Parser("dbinstance")
void dbinstance(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json?.get(0).each { name, value ->
        csv << [name, value]
        info["dbinstance.${name.toLowerCase()}"] = value
    }
    t.devices(['name', 'value'], csv)
    t.results(info)
}

@Parser("hostconfig")
void hostconfig(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json?.each { row ->
        def name = row.get("LOWER(STAT_NAME)")
        def value = row.get("VALUE")
        def name2 = name.replaceAll(/.+:/,"")
        csv << [name, value]
        if (name == 'physical_memory_bytes') {
            def memory_mb = value as Double / (1024 * 1024)
            info["hostconfig.physical_memory_mb"] = memory_mb.round(0)
        }
        info["hostconfig.${name}"] = value
    }
    t.devices(['name', 'value'], csv)
    t.results(info)
}

@Parser("dbcomps")
void dbcomps(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def versions = [:]
    def csv = []
    json?.each { row ->
        def name = row.get("COMP_NAME")
        def version = row.get("VERSION")
        if (name =~/Real Application Clusters/) {
            info["dbcomps.RAC"] = version
        }
        info["dbcomps.${name}"] = version
        versions[version] = true
        csv << [name, version, row.get("STATUS")]
    }
    info['dbcomps.version'] = versions.keySet().join(',')
    t.devices(['name', 'version', 'status'], csv)
    t.results(info)
}

@Parser("dbfeatusage")
void dbfeatusage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    json?.each { row ->
        csv << [row.get("NAME"), row.get("DETECTED_USAGES"), 
                row.get("CURRENTLY_USED"), row.get("VERSION")]
    }
    t.devices(['name', 'detected_usages', 'currently_used', 'version'], 
              csv)
}

def check_memory_management(Map info){
    def memory_max_target = NumberUtils.toDouble(info["dbinfo.memory_max_target"])
    def memory_target     = NumberUtils.toDouble(info["dbinfo.memory_target"])
    def sga_target        = NumberUtils.toDouble(info["dbinfo.sga_target"])
    def statistics_level  = info['dbinfo.statistics_level'].toLowerCase()

    if (statistics_level) {
        if (memory_max_target > 0 && memory_target > 0) {
            return 'AMM'
        } else if (sga_target > 0 && (statistics_level == 'typical' ||
                   statistics_level == 'all')) {
            return 'ASMM'
        } else {
            return 'None'
        }
    } else {
        return 'unkown'
    }
}

@Parser("dbinfo")
void dbinfo(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def name = row.get("NAME")
        def value = row.get("VALUE") ?: 'N/A'
        info["dbinfo.${name}"] = value
        csv << [name, value]
    }
    info["dbinfo.memory_management"] = check_memory_management(info)
    t.devices(['name', 'value'], csv)
    t.results(info)
}

@Parser("dbvers")
void dbvers(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    json.each { row ->
        def product = row.get("PRODUCT")
        def version = row.get("VERSION")
        def status = row.get("STATUS")
        (product=~/Oracle Database/).each {
            t.setMetric('dbvers.Oracle Database', "$product $version $status")
        }
        csv << [product, version, status]
    }
    t.devices(['product', 'version', 'status'], csv)
}

@Parser("nls")
void nls(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def name = row.get("LOWER(NAME)")
        def value = row.get("VALUE")
        def comment = row.get("comment")
        csv << [name, value, comment]
        info["nls.${name}"] = value
    }
    t.devices(['name', 'value', 'comment'], csv)
    t.results(info)
}

@Parser("dbstorage")
void dbstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    json.each { row ->
        def name = row.get("TABLESPACE_NAME")
        def size = row.get("size(MB)") as Double
        def used = row.get("used(MB)") as Double
        def free = row.get("free(MB)") as Double
        def rate = row.get("rate(%)") as Double
        csv << [name, size, used, free, rate]
    }
    t.devices(['name', 'size', 'used', 'free', 'rate'], csv)
}

@Parser("redoinfo")
void redoinfo(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def csv = []
    int redo_size = 0
    int redo_count = 0
    def is_mirror = false
    json.each { row ->
        def group = row.get("GROUP#")
        def thread = row.get("THREAD#")
        def member = row.get("MEMBERS")
        def bytes = row.get("BYTES")
        def status = row.get("STATUS")
        def archived = row.get("ARCHIVED")
        def first_change = row.get("FIRST_CHANGE#")
        def first_time = row.get("FIRST_TIME")
        if (member > 1)
            is_mirror = true
        redo_size = bytes
        redo_count ++

        csv << [group, thread, member, bytes, status, archived, 
                first_change, first_time]
    }
    t.setMetric("redoinfo.redo_size", redo_size)
    t.setMetric("redoinfo.redo_count", redo_count)
    t.setMetric("redoinfo.redo_mirror", is_mirror)

    t.devices(['group', 'thread', 'member', 'bytes', 'status', 
               'archived', 'first_change', 'first_time'], csv)
}

@Parser("sgasize")
void sgasize(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def name = row.get("NAME")
        def value = row.get("VALUE") ?: 'N/A'
        info["sgasize.${name}"] = value
        csv << [name, value]
    }
    t.devices(['name', 'value'], csv)
    t.results(info)
}

@Parser("sysmetric")
void sysmetric(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def begin = row.get('BEGIN_TIME')
        def end = row.get('END_TIME')
        def value = row.get('VALUE')
        def name = row.get('METRIC_NAME')
        info["sysmetric.${name}"] = value
        csv << [begin, end, name, value]
    }
    t.devices(['begin', 'end', 'name', 'value'], csv)
    t.results(info)
}

@Parser("systime")
void systime(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def name = row.get('STAT_NAME')
        def second = row.get('SECONDS')
        def minute = row.get('MINUTES')
        def id = name.replaceAll(/\s+/, "")
        if (id != "") {
            info["systime.${id}"] = second
            csv << [name, second, minute]
        }
    }
    t.devices(['name', 'second', 'minute'], csv)
    t.results(info)
}

@Parser("tabstorage")
void tabstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def owner = row.get('OWNER')
        def name = row.get('TABLE_NAME')
        def mbytes = row.get('MBYTES')
        info["tabstorage.${name}"] = mbytes
        csv << [owner, name, mbytes]
    }
    t.devices(['owner', 'name', 'mbytes'], csv)
    t.results(info)
}

@Parser("sumstorage")
void sumstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = [:]
    def csv = []
    json.each { row ->
        def type = row.get('TYPE')
        def total = row.get('TOTAL')
        info["sumstorage.${type}"] = total
        csv << [type, total]
    }
    t.devices(['type', 'total'], csv)
    t.results(info)
}

