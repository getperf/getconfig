package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "GconfExecuterTest.初期化"

class AgentExecuterTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'

     def "初期化"() {
         when:
         TestServer server = new TestServer(serverName:"centos80",
                 domain:"Linux",
                 ip:"192.168.10.1",
                 accountId:"Account01")
         def executer = new AgentExecuter("Linux", server)
         ConfigEnv.instance.accept(executer)

         then:
         println executer.args()
         executer.toml().size() > 0
         executer.gconfExe.size() > 0
     }

    def "不明プラットフォーム初期化"() {
        when:
        TestServer server = new TestServer(serverName:"centos80",
                domain:"Linux",
                ip:"192.168.10.1",
                accountId:"Account01")
        new AgentExecuter("Hoge", server)

        then:
        thrown(IllegalArgumentException)
    }

}
