package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification

// gradle --daemon test --tests "GconfExecuterTest.エージェントコマンド実行"

class LocalAgentExecutorTest extends Specification {
    TestServer server = new TestServer(serverName:"centos80",
            domain:"Linux",
            ip:"127.0.0.1",
            user:"psadmin",
            password:"psadmin",
            accountId:"Account01")

    def "初期化"() {
         when:
         def executor = new LocalAgentExecutor("Linux", server)
         ConfigEnv.instance.accept(executor)

         then:
         println executor.args()
         println executor.toml()
         executor.toml().size() > 0
         executor.gconfExe.size() > 0
     }

    def "TOML読込み"() {
        when:
        def executor = new LocalAgentExecutor("Linux", server)
        ConfigEnv.instance.accept(executor)
        executor.metricLib = 'src/test/resources/lib/dictionary'

        then:
        executor.getMetricLibsText().size() > 0
    }

    def "不明プラットフォーム初期化"() {
        when:
        new LocalAgentExecutor("Hoge", server)

        then:
        thrown(IllegalArgumentException)
    }

    def "エージェントコマンド実行"() {
        when:
        def executer = new LocalAgentExecutor("Linux", server)
        ConfigEnv.instance.accept(executer)
        def rc =executer.run()

        then:
        rc == 0
    }

    def "リモートエージェント初期化"() {
        when:
        def executer = new RemoteAgentExecutor(server)
        executer.currentLogDir = "build/log"
        executer.tlsConfigDir = "config/network"

        then:
        println executer.args()
        executer.args().size() > 0
    }
}
