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
    println json
}

@Parser("dbinstance")
void dbinstance(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("hostconfig")
void hostconfig(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("dbcomps")
void dbcomps(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("dbfeatusage")
void dbfeatusage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("dbinfo")
void dbinfo(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("dbvers")
void dbvers(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("nls")
void nls(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("dbstorage")
void dbstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("redoinfo")
void redoinfo(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("sgasize")
void sgasize(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("sysmetric")
void sysmetric(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("systime")
void systime(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("tabstorage")
void tabstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

@Parser("sumstorage")
void sumstorage(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
}

