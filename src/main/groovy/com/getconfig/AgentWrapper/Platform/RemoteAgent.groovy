package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic
@InheritConstructors
class RemoteAgent {
    String getLabel() {
        return "remoteagent"
    }

    String getConfigName() {
        return "remoteagent"
    }

    AgentCommandConfig convertAll(List<TestServer> servers) {
    }

    AgentCommandConfig convert(TestServer server) {
        def config = new AgentCommandConfig(
                server: 'localhost',
                local_exec: false,
        )
        return config
    }
}
