package com.getconfig.AgentWrapper

import spock.lang.Specification

class ConfigWrapperContextTest extends Specification {

    def "初期化"() {
        when:
        def env = ConfigWrapperContext.instance
        def agentConfigWrapper = env.getWrapper("Linux")

        then:
        println agentConfigWrapper.getLabel() == "linuxconf"
    }

    def "不明プラットフォームの初期化"() {
        when:
        def env = ConfigWrapperContext.instance
        env.getWrapper("Hoge")

        then:
        thrown(IllegalArgumentException)
    }

}
