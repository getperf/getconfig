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

    def "encript"() {
        when:
        String fileContents = new File(configFile).text
        // Config.readConfig('./config/config.groovy')
        Config.instance.encrypt(configFile, "P@ssw0rd")
        Config.instance.decrypt(configFile + "-encrypted", "P@ssw0rd")

        then:
        new File(configFile).text == fileContents
    }
}
