package com.getconfig.AgentWrapper


import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import java.text.SimpleDateFormat

// gradle --daemon test --tests "LinuxTest.gconf設定ファイル変換2"

class OracleTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    AgentConfigWrapper wrapper

    def setup() {
        wrapper = agentWrapperManager.getWrapper("Oracle")
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
        com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"hoge",
            domain:"Oracle",
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
                domain:"Oracle",
                ip:"192.168.10.1",
                accountId:"Account01")
        PlatformMetric metrics = TomlUtils.read("lib/dictionary/Oracle.toml", PlatformMetric)

        def gconf = wrapper.makeServerConfig(server)
        // gconf に　メトリックは追加せずに、個別に metrics を追加する
//        gconf.metrics = metrics
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)

        then:
        println toml
        toml.size() > 0
    }

}
