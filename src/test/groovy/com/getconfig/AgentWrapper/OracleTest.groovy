package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import java.text.SimpleDateFormat

// gradle --daemon test --tests "LinuxTest.gconf設定ファイル変換2"

class OracleTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    DirectExecutorWrapper wrapper
    Server server

    def setup() {
        server = new com.getconfig.Model.Server(serverName:"hoge",
                domain:"Oracle",
                ip:"192.168.10.1",
                accountId:"Account01")
        DirectExecutor executor = new DirectExecutor("Oracle", server)
        ConfigEnv.instance.accept(executor)
        executor.setPlatformMetricFromLibs()

        wrapper = agentWrapperManager.getDirectExecutorWrapper("Oracle")
        executor.accept(wrapper)
    }

    def "Oracle ラッパー初期化"() {
        when:
        def label = wrapper.getLabel()

        then:
        label == "oracleconf"
    }

    def "Oracle ラッパー実行"() {
        when:
        wrapper.dryRun()

        then:
        1 == 1
    }
}
