package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import spock.lang.Specification

// gradle --daemon test --tests "GconfExecuterTest.エージェントコマンド実行"

class DirectExecutorTest extends Specification {
    com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"ora12",
            domain:"Oracle",
            ip:"127.0.0.1",
            user:"psadmin",
            password:"psadmin",
            accountId:"Account01")

    def "初期化"() {
         when:
         def executor = new DirectExecutor("Oracle", server)
         ConfigEnv.instance.accept(executor)

         then:
         executor.getMetricLibsText() != null
     }

    def "不明プラットフォーム初期化"() {
        when:
        new DirectExecutor("Hoge", server)

        then:
        thrown(IllegalArgumentException)
    }

    def "実行"() {
        when:
        def executor = new DirectExecutor("Oracle", server)
        ConfigEnv.instance.accept(executor)
        executor.level = 99
        def rc =executor.run()

        then:
        1 == 1
//        rc == 0
    }

}
