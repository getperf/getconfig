package com.getconfig

import spock.lang.Specification
import com.getconfig.Model.*
import com.getconfig.Document.*

// gradle --daemon test --tests "LogParserTest.初期化"

class LogParserTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'
    String currentLogDir = './src/test/resources/inventory'
    List<TestServer> testServers

    def setup() {
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.mergeConfig()
        testServers = specReader.testServers()
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
}
