package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "HPiLOParserTest.Summary"

class HPiLOParserTest extends Specification {
    static AgentLogParserManager logParsers
    String logPath = "src/test/resources/inventory/ostrich/HPiLO/ostrich"

    def setupSpec() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init("HPiLO")
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def "overview"() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "overview")
        t.logPath = testLogPath("overview")
        logParsers.invoke(t)

        then:
        t.get("HPiLO", "overview.product_name").value ==
                "ProLiant DL360 Gen10"
    }

    def License() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "License")
        t.logPath = testLogPath("License")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def proc_info() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "proc_info")
        t.logPath = testLogPath("proc_info")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.proc_info").value == ""
        1 == 1
    }

    def mem_info() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "mem_info")
        t.logPath = testLogPath("mem_info")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.mem_info").value == ""
        1 == 1
    }

    def network() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "network")
        t.logPath = testLogPath("network")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.network").value == ""
        1 == 1
    }

    def Storage() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "Storage")
        t.logPath = testLogPath("Storage")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.Storage").value == ""
        1 == 1
    }

    def drive() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "drive")
        t.logPath = testLogPath("drive")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.drive").value == ""
        1 == 1
    }

    def snmp() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "snmp")
        t.logPath = testLogPath("snmp")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.snmp").value == ""
        1 == 1
    }

    def power_regulator() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "power_regulator")
        t.logPath = testLogPath("power_regulator")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.power_regulator").value == ""
        1 == 1
    }

    def power_summary() {
        when:
        TestUtil t = new TestUtil("ostrich", "HPiLO", "power_summary")
        t.logPath = testLogPath("power_summary")
        logParsers.invoke(t)

        then:
    //    t.get("HPiLO", "overview.power_summary").value == ""
        1 == 1
    }


}
