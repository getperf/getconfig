package com.getconfig.AgentLogParser

import com.getconfig.TestItem
import spock.lang.Specification

class AgentLogParserManagerTest extends Specification {
    AgentLogParserManager logParsers

    def setup() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init()
    }

    def "uname"() {
        when:
        TestItem testItem = new TestItem("cent80", "Linux", "uname")
        testItem.logPath = "src/test/resources/inventory/centos80/Linux/centos80/uname"

        then:
        logParsers.invoke(testItem)
        1 == 1
    }

    def "network"() {
        when:
        TestItem testItem = new TestItem("cent80", "Linux", "network")
        testItem.logPath = "src/test/resources/inventory/centos80/Linux/centos80/network"

        then:
        logParsers.invoke(testItem)
        1 == 1
    }
}
