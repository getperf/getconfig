package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestMetric

// @Slf4j
@CompileStatic
@InheritConstructors
class LocalAgent implements AgentConfigWrapper {
    @Override
    String getLabel() {
        return "localagent"
    }

    @Override
    String getConfigName() {
        return "localagent"
    }

    @Override
    boolean getBatchEnable() {
        return true
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers, List<TestMetric> testMetrics) {
        return new AgentCommandConfig()
    }

    @Override
    def makeServerConfig(TestServer server, List<TestMetric> testMetrics) {
        return new AgentCommandConfig()
    }
}
