package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestMetric

// @Slf4j
@CompileStatic
@InheritConstructors
class RemoteAgent implements AgentConfigWrapper {
    @Override
    String getLabel() {
        return "remoteagent"
    }

    @Override
    String getConfigName() {
        return "remoteagent"
    }

    @Override
    boolean getBatchEnable() {
        return false
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers, List<TestMetric> testMetrics) {
        return null
    }

    @Override
    def makeServerConfig(TestServer server, List<TestMetric> testMetrics) {
        def config = new AgentCommandConfig(
                server: 'localhost',
                local_exec: false,
        )
        return config
    }
}
