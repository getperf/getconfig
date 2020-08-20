package com.getconfig.AgentWrapper

import spock.lang.Specification

class AgentWrapperManagerTest extends Specification {

    def "初期化"() {
        when:
        def env = AgentWrapperManager.instance
        def agentConfigWrapper = env.getWrapper("Linux")

        then:
        agentConfigWrapper.getLabel() == "linuxconf"
    }

    def "不明プラットフォームの初期化"() {
        when:
        def env = AgentWrapperManager.instance
        env.getWrapper("Hoge")

        then:
        thrown(IllegalArgumentException)
    }

}
