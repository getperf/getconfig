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

        Collector collector = new Collector(testServers)
        collector.run()

        then:
        collector.testServers.size() > 0
    }

}
