package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "CiscoUCSParserTest.Summary"

class CiscoUCSParserTest extends Specification {
    static AgentLogParserManager logParsers
    String logPath = "src/test/resources/inventory/centos7g/CiscoUCS/centos7g"

    def setupSpec() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init("CiscoUCS")
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def bios() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "bios")
        t.logPath = testLogPath("bios")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def chassis() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "chassis")
        t.logPath = testLogPath("chassis")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def cimc() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "cimc")
        t.logPath = testLogPath("cimc")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def cpu() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "cpu")
        t.logPath = testLogPath("cpu")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def memory() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "memory")
        t.logPath = testLogPath("memory")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def hdd() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "hdd")
        t.logPath = testLogPath("hdd")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def storageadapter() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "storageadapter")
        t.logPath = testLogPath("storageadapter")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def physical_drive() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "physical_drive")
        t.logPath = testLogPath("physical_drive")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def virtual_drive() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "virtual_drive")
        t.logPath = testLogPath("virtual_drive")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def network() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "network")
        t.logPath = testLogPath("network")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def snmp() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "snmp")
        t.logPath = testLogPath("snmp")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def snmp_trap() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "snmp_trap")
        t.logPath = testLogPath("snmp_trap")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def ntp() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "ntp")
        t.logPath = testLogPath("ntp")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def system() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "system")
        t.logPath = testLogPath("system")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def storage() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "storage")
        t.logPath = testLogPath("storage")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def subsystem_health() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "subsystem_health")
        t.logPath = testLogPath("subsystem_health")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    
    def system_node() {
        when:
        TestUtil t = new TestUtil("centos7g", "CiscoUCS", "system_node")
        t.logPath = testLogPath("system_node")
        logParsers.invoke(t)

        then:
        1 == 1
    }
    

}
