package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "VMHostParserTest.Summary"

class VMHostParserTest extends Specification {
    AgentLogParserManager logParsers
    // String logPath = "src/test/resources/inventory/w2016/Windows/w2016"
    String logPath = "src/test/resources/inventory/LocalAgentBatch_VMHost_192.168.10.100_Account01/esxi.ostrich"
    def setup() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init()
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def "Summary"() {
        when:
        TestUtil t = new TestUtil("esxi.ostrich", "VMHost", "summary.json")
        t.logPath = testLogPath("summary.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }

    def "Config"() {
        when:
        TestUtil t = new TestUtil("esxi.ostrich", "VMHost", "config.json")
        t.logPath = testLogPath("config.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "ConfigManager"() {
        when:
        TestUtil t = new TestUtil("esxi.ostrich", "VMHost", "configManager.json")
        t.logPath = testLogPath("configManager.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }

    def "Capability"() {
        when:
        TestUtil t = new TestUtil("esxi.ostrich", "VMHost", "capability.json")
        t.logPath = testLogPath("capability.json")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
}
