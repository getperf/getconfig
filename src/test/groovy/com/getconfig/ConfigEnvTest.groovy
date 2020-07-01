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
        env.getCmdbConfigPath().size() > 0
    }

    def "アカウントセット"() {
        when:
        def configEnv = ConfigEnv.instance
        configEnv.readConfig(configFile)
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        configEnv.setAccont(server)

        then:
        server.user == "someuser"
    }

    def "不明アカウントセット"() {
        when:
        def configEnv = ConfigEnv.instance
        configEnv.readConfig(configFile)
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"AccountHoge")
        configEnv.setAccont(server)

        then:
        thrown(IllegalArgumentException)
    }

    def "Gconfラッパー初期化"() {
        when:
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        env.loadGconfWrapper()

        def agentConfigWrapper = env.getAgentConfigWrapper("Linux")

        then:
        println agentConfigWrapper
        1 == 1
    }

}
