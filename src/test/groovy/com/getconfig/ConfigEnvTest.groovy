package com.getconfig

import com.getconfig.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "ConfigEnvTest.Gconfラッパー初期化"

class ConfigEnvTest extends Specification {
    String configFile = './src/test/resources/config/config.groovy'

    def "初期化"() {
        when:
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        println env.getProjectLogDir()

        then:
        env.getInventoryDBConfigPath().size() > 0
    }

    def "アカウントセット"() {
        when:
        def configEnv = ConfigEnv.instance
        configEnv.readConfig(configFile)
        Server server = new Server(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        configEnv.setAccount(server)

        then:
        server.user == "someuser"
    }

    def "不明アカウントセット"() {
        when:
        def configEnv = ConfigEnv.instance
        configEnv.readConfig(configFile)
        Server server = new Server(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"AccountHoge")
        configEnv.setAccount(server)

        then:
        thrown(IllegalArgumentException)
    }

    def "タイムアウト取得"() {
        when:
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        def timeout = env.getGconfTimeout()

        then:
        println timeout
        1 == 1
    }

    def "既定値取得"() {
        when:
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        println env.getTlsConfigDir()

        then:
        env.getProjectName() == "getconfig"
        env.getGetconfigExe() =~ /getconfig/
        env.getCheckSheetPath() =~ /getconfig\.xlsx/
        env.getTlsConfigDir() =~ /network/
        env.getGconfTimeout("Hoge") == 120
        env.getPassword() == null
        env.getLevel() == 0
        env.getAutoTagFlag() == false
        env.getAutoTagNumber() == 10
        env.getKeywordServer() == null
        env.getKeywordTest() == null
        env.getKeywordPlatform() == null
        env.getSilent() == false
        env.getZipPath() == null
        env.getAllFlag() == true
        env.getTargetType() == null
        env.getRedmineProject() == null
    }
}
