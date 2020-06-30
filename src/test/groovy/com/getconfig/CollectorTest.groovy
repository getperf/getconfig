package com.getconfig

import spock.lang.Specification
import com.getconfig.Model.*
import com.getconfig.Document.*

// gradle --daemon test --tests "CollectorTest.初期化"

class CollectorTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'

    def "初期化"() {
        when:
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.mergeConfig()
        def testServers = specReader.testServers()

        Collector collector = new Collector(
             gconfExe : env.getGconfExe(),
             tlsConfigDir : env.getTlsConfigDir(),
             currentLogDir : env.getCurrentLogDir(),
             gconfConfigDir : env.getGconfConfigDir(),
             testServers : testServers,
        )
        // collector.init()

        then:
        collector.testServers.size() > 0
    }

    // def "収集設定ファイル作成"() {
    //     when:
    //     Collector collector = new Collector(checkSheet, configFile)
    //     collector.init()
    //     collector.makeConfig()

    //     then:
    //     collector.testServers.size() > 0
    // }

}
