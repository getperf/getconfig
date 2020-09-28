package com.getconfig.AgentWrapper


import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import java.text.SimpleDateFormat

// gradle --daemon test --tests "LinuxTest.gconf設定ファイル変換2"

class LinuxTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    AgentConfigWrapper wrapper

    def setup() {
        wrapper = agentWrapperManager.getWrapper("Linux")
    }

    def "gconfコマンド用構造体変換"() {
        when:
        com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        def gconf = wrapper.makeServerConfig(server)

        then:
        gconf.server == 'localhost'
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
        com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"hoge",
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
        com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"hoge",
                domain:"Linux",
                ip:"192.168.10.1",
                accountId:"Account01")
        PlatformMetric metrics = TomlUtils.read("lib/dictionary/Linux.toml", PlatformMetric)

        def gconf = wrapper.makeServerConfig(server)
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)

        then:
        println toml
        toml.size() > 0
    }

    def "日付変換"() {
        when:
        def install_time = Long.decode("12345") * 1000L
//        def dt = new Date(install_time).format("yyyy/MM/dd HH:mm:ss")
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        def dt = df.format(new Date(install_time))

        then:
        println dt
        1 == 1

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
