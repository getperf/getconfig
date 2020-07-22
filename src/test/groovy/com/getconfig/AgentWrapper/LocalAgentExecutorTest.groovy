package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "GconfExecuterTest.初期化"

class LocalAgentExecutorTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'
    TestServer server = new TestServer(serverName:"centos80",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")

     def "初期化"() {
         when:
         def executer = new LocalAgentExecutor("Linux", server)
         ConfigEnv.instance.accept(executer)

         then:
         println executer.args()
         executer.toml().size() > 0
         executer.gconfExe.size() > 0
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
