package com.getconfig.Document

import com.getconfig.Model.*
import com.getconfig.ConfigEnv
import spock.lang.Specification

// gradle --daemon test --tests "SpecReaderTest.設定ファイルとのマージ"

class SpecReaderTest extends Specification {

    String checkSheet = './src/test/resources/getconfig.xlsx'
    String paramTestSheet = './src/test/resources/getconfig_param_test.xlsx'
    String configFile = './src/test/resources/config/config.groovy'

    def "初期化"() {
        when:
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.print()

        then:
        specReader.serverCount() > 0
    }

    def "不明ファイル"() {
        when:
        def specReader = new SpecReader(inExcel : './hoge.xlsx')
        specReader.parse()

        then:
        thrown(FileNotFoundException)
    }

    def "設定ファイルとのマージ"() {
        ConfigEnv.instance.readConfig(configFile)

        when:
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.mergeConfig()
        def servers = specReader.testServers()
//        println servers

        then:
        specReader.print()
        specReader.serverCount() > 0
        servers[0].user == "someuser"
        servers[0].password == "P@ssword"
    }

    def "プラットフォームパラメータ読込み"() {
        when:
        def specReader = new SpecReader(inExcel : paramTestSheet)
        specReader.parse()

        then:
        specReader.platformParameters.get('Packages').values.size() > 0
        specReader.platformParameters.get('Hoge').values[0] == 1
        specReader.platformParameters.get('None').values.size() == 0
    }
}
