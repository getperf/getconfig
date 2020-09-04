package com.getconfig

import spock.lang.Specification
import com.getconfig.Model.*
import com.getconfig.Document.*
import com.getconfig.AgentWrapper.AgentWrapperManager

// gradle --daemon test --tests "CollectorTest.初期化"

class CollectorTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'
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
        Collector collector = new Collector(testServers)

        then:
        collector.testServers.size() > 0
    }

    def "検査対象分類"() {
        when:
        Collector collector = new Collector(testServers)
        collector.classifyTestServers()

        then:
        collector.testServerGroups.size() > 0
    }

    def "HUBサーバローカルファイル処理"() {
        when:
        Collector collector = new Collector(testServers)
        collector.filterServer = "server02"
        collector.classifyTestServers()
        collector.runAgent()

        then:
        collector.testServers.size() > 0
    }

    def "リモートエージェント処理"() {
        when:
        Collector collector = new Collector(testServers)
        collector.filterServer = "centos80"
        collector.classifyTestServers()
        collector.runAgent()

        then:
        collector.testServers.size() > 0
    }


    def "ドライラン"() {
        when:
        Collector collector = new Collector(testServers)
        ConfigEnv.instance.accept(collector)
        collector.dryRun = true
        def rc = collector.run()

        then:
        rc == 0
    }

}
