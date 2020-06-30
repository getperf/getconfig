package com.getconfig.Document

import com.getconfig.Model.*
import com.getconfig.ConfigEnv
import spock.lang.Specification

// gradle --daemon test --tests "SpecReaderTest.設定ファイルとのマージ"

class SpecReaderTest extends Specification {

    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
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

        then:
        specReader.print()
        specReader.serverCount() > 0
        servers[0].user == "someuser"
        servers[0].password == "P@ssword"
    }
}
