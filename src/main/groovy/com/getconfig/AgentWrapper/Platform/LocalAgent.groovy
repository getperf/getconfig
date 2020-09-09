package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

// @Slf4j
@TypeChecked
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
    def makeAllServersConfig(List<Server> servers) {
        return new AgentCommandConfig()
    }

    @Override
    def makeServerConfig(Server server) {
        return new AgentCommandConfig()
    }
}
