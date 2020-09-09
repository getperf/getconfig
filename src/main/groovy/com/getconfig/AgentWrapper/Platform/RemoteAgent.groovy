package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

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
    def makeAllServersConfig(List<Server> servers) {
        return null
    }

    @Override
    def makeServerConfig(Server server) {
        def config = new AgentCommandConfig(
                server: 'localhost',
                local_exec: false,
        )
        return config
    }
}
