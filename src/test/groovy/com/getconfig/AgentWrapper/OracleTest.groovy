package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import java.text.SimpleDateFormat

// gradle --daemon test --tests "OracleTest.Oracle ラッパー実行"

class OracleTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    DirectExecutorWrapper wrapper
    Server server

    def setup() {
        wrapper = agentWrapperManager.getDirectExecutorWrapper("Oracle")
    }

    def "Oracle ラッパー初期化"() {
        when:
        def label = wrapper.getLabel()

        then:
        label == "oracleconf"
    }

    def "Oracle ラッパー実行"() {
        when:
        server = new com.getconfig.Model.Server(serverName:"ora12",
                domain:"Oracle",
                ip:"192.168.24.72:11521",
                user:"scott",
                password:"tiger",
                remoteAlias:"TEST1")
        DirectExecutor executor = new DirectExecutor("Oracle", server)
        ConfigEnv.instance.accept(executor)
        executor.setPlatformMetricFromLibs()
        executor.makeAgentLogDir()
        executor.accept(wrapper)

        // wrapper.run()
        wrapper.dryRun()

        then:
        1 == 1
    }
}
