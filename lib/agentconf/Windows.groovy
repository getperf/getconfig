package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestMetric

// @Slf4j
@CompileStatic
@InheritConstructors
class Windows implements AgentConfigWrapper {
    @Override
    String getLabel() {
        return "windowsconf"
    }

    @Override
    String getConfigName() {
        return "windowsconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return false
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers, List<TestMetric> testMetrics) {
        // TODO: Create specification
    }

    @Override
    def makeServerConfig(TestServer server, List<TestMetric> testMetrics) {
        def config = new AgentCommandConfig(
                server: 'localhost',
                local_exec: false,
                servers: [
                        new Server(
                                server : server.serverName,
                                url : server.ip,
                                user : server.user,
                                password : server.password,
                                ssh_key : server.loginOption,
                                insecure : true,
                        ),
                ],
        )
        return config
    }
}
