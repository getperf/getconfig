package com.getconfig

import spock.lang.Specification
import com.getconfig.Model.*
import com.getconfig.Document.*

// gradle --daemon test --tests "CollectorTest.初期化"

class CollectorTest extends Specification {
    String checkSheet = './src/test/resources/getconfig.xlsx'
    String configFile = './src/test/resources/config/config.groovy'
    List<Server> testServers

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
        ConfigEnv.instance.accept(collector)
        collector.filterServer = "server02"
        collector.classifyTestServers()
        collector.runAgent()

        then:
        collector.testServers.size() > 0
    }

    def "フィルター処理(サーバ)"() {
        when:
        Collector collector = new Collector(testServers)
        ConfigEnv.instance.accept(collector)
        collector.filterServer = "centos80"
        collector.classifyTestServers()

        then:
        collector.testServerGroups.each {key, serverGroup ->
            println key
            println serverGroup.testServers*.serverName
            println serverGroup.testServers*.domain
        }
        1 == 1
    }

    def "フィルター処理(プラットフォーム)"() {
        when:
        Collector collector = new Collector(testServers)
        ConfigEnv.instance.accept(collector)
        collector.filterServer = "centos80"
        collector.filterPlatform = "Linux"
        collector.classifyTestServers()

        then:
        collector.testServerGroups.each {key, serverGroup ->
            println key
            println serverGroup.testServers*.serverName
            println serverGroup.testServers*.domain
        }
        1 == 1
    }

    def "リモートエージェント処理"() {
        when:
        Collector collector = new Collector(testServers)
        ConfigEnv.instance.accept(collector)
        collector.filterServer = "centos80"
        collector.classifyTestServers()
        collector.runAgent()

        then:
        collector.testServers.size() > 0
    }

    def "ダイレクト実行"() {
        when:
        List<Server> oracleServers = TestData.readTestServers("oracle")
        Collector collector = new Collector(oracleServers)
        ConfigEnv.instance.accept(collector)
//        collector.filterServer = "server02"
        collector.classifyTestServers()
//        collector.runAgent()

        then:
        println oracleServers
        println collector.testServerGroups
//        collector.testServers.size() > 0
        1 == 1
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
