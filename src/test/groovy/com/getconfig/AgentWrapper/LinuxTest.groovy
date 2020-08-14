package com.getconfig.AgentWrapper


import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import com.getconfig.AgentWrapper.*

// gradle --daemon test --tests "LinuxTest.gconf設定ファイル変換2"

class LinuxTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    AgentConfigWrapper wrapper

    def setup() {
        agentWrapperManager.init("lib/agentconf")
        wrapper = agentWrapperManager.getWrapper("Linux")
    }

    def "gconfコマンド用構造体変換"() {
        when:
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        def gconf = wrapper.makeServerConfig(server)

        then:
        gconf.server == 'localhost'
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        def gconf = wrapper.makeServerConfig(server)
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)

        then:
        println toml
        toml.size() > 0
    }

    def "設定ファイル変換メトリック付き"() {
        when:
        TestServer server = new TestServer(serverName:"hoge",
                domain:"Linux",
                ip:"192.168.10.1",
                accountId:"Account01")
        TestMetricGroup metrics = TomlUtils.read("lib/dictionary/Linux.toml", TestMetricGroup)

        def gconf = wrapper.makeServerConfig(server, metrics.getAll())
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)

        then:
        println toml
        toml.size() > 0
    }

//    def "gconf設定ファイル変換2"() {
//        when:
//         TestServer server = new TestServer(serverName:"hoge",
//            domain:"Linux",
//            ip:"192.168.10.1",
//            accountId:"Account01")
//
//        def gconfInit = GconfInitializer.instance.init()
//        def converter = gconfInit.getConverter(server.domain)
//        AgentCommandConfig gconf = converter.convert(server)
//        TomlWriter tomlWriter = new TomlWriter()
//        def toml = tomlWriter.write(gconf)
//
//        then:
//        toml.size() > 0
//    }
}
