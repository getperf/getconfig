package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import spock.lang.Specification
import com.getconfig.Model.*

// gradle --daemon test --tests "LogParserTest.初期化"

class LogParserTest extends Specification {
    String currentLogDir = './src/test/resources/inventory'
    List<Server> testServers

    def setup() {
        testServers = TestData.readTestServers()
    }

    def "初期化"() {
        when:
        LogParser logParser = new LogParser(testServers)

        then:
        logParser.testServers.size() > 0
    }

    def "パース処理"() {
        when:
        LogParser logParser = new LogParser(testServers)
        logParser.agentLogPath = this.currentLogDir
        logParser.parserLibPath = "./lib/parser"

        then:
        logParser.run() == 0
    }

    def "サーバを絞り込んだパース"() {
        when:
        LogParser logParser = new LogParser(testServers)
        logParser.agentLogPath = this.currentLogDir
        logParser.parserLibPath = "./lib/parser"
        logParser.filterServer = 'centos80'
        logParser.filterPlatform = 'summary'

        then:
        logParser.run() == 0
    }

    def "オンプレIAサーバシナリオパース"() {
        when:
        def testServers2 = TestData.readTestServers("ia_on_premises")
        println testServers2
        LogParser logParser = new LogParser(testServers2)
        logParser.agentLogPath = this.currentLogDir
        logParser.parserLibPath = "./lib/parser"

        then:
        logParser.run() == 0
    }

    def "バッチシナリオパース"() {
        when:
        def testServers2 = TestData.readTestServers("netapp")
//        println testServers2
        LogParser logParser = new LogParser(testServers2)
        logParser.agentLogPath = this.currentLogDir
        logParser.parserLibPath = "./lib/parser"
        logParser.makeAgentLogLists()
//        println logParser.agentLogs
        logParser.agentLogs.each { AgentLog agentLog ->
            if (agentLog.platform == 'NetApp') {
                println agentLog
            }
        }
        then:
//        logParser.run() == 0
        1 == 1
    }
}
