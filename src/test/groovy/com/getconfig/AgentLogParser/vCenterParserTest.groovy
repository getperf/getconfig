package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "VCenterParserTest.Summary"

class VCenterParserTest extends Specification {
    AgentLogParserManager logParsers
    // String logPath = "src/test/resources/inventory/w2016/Windows/w2016"
    String logPath = "src/test/resources/inventory/LocalAgentBatch_vCenter_192.168.10.100_Account01/centos80"
    def setup() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init()
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def "Summary"() {
        when:
        TestUtil t = new TestUtil("centos80", "vCenter", "summary.json")
        t.logPath = testLogPath("summary.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "ResourceConfig"() {
        when:
        TestUtil t = new TestUtil("centos80", "vCenter", "resourceConfig.json")
        t.logPath = testLogPath("resourceConfig.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }

    def "Config"() {
        when:
        TestUtil t = new TestUtil("centos80", "vCenter", "config.json")
        t.logPath = testLogPath("config.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }

    def "Guest"() {
        when:
        TestUtil t = new TestUtil("centos80", "vCenter", "guest.json")
        t.logPath = testLogPath("guest.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
}
