package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic
@InheritConstructors
class LocalAgent implements AgentConfigWrapper {
    @Override
    String getLabel() {
        return "localeagent"
    }

    @Override
    String getConfigName() {
        return "localeagent"
    }

    @Override
    boolean getBatchEnable() {
        return true
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers) {
        return new AgentCommandConfig()
    }

    @Override
    def makeServerConfig(TestServer server) {
        return new AgentCommandConfig()
    }
}
