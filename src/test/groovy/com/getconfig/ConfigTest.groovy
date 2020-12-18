package com.getconfig

import spock.lang.Specification
import com.getconfig.Model.*

// gradle --daemon test --tests "ConfigTest.初期化"

class ConfigTest extends Specification {
    String configFile = './src/test/resources/config/config.groovy'

    def "初期化"() {
        when:
        ConfigObject config = Config.instance.readConfig(configFile)

        then:
        1 == 1
        println config
    }

    def "複数設定ファイル初期化"() {
        when:
        ConfigObject config = Config.instance.readConfig(configFile,
                null, true)

        then:
        config.account?.Zabbix?.Account01?.password == "P@ssw0rd"
        config.account?.Zabbix?.Account02?.user == "Admin2"
    }

    def "encript"() {
        when:
        String fileContents = new File(configFile).text
        // Config.readConfig('./config/config.groovy')
        Config.instance.encrypt(configFile, "P@ssw0rd")
        Config.instance.decrypt(configFile + "-encrypted", "P@ssw0rd")

        then:
        new File(configFile).text == fileContents
    }

    def "複数設定ファイルencript"() {
        when:
        String fileContents = new File(configFile).text
        // Config.readConfig('./config/config.groovy')
        Config.instance.encrypt(configFile, "P@ssw0rd", true)
        Config.instance.decrypt(configFile + "-encrypted", "P@ssw0rd", true)

        then:
        new File(configFile).text == fileContents
    }
}
