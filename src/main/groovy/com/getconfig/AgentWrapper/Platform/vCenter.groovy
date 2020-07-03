package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic
@InheritConstructors
class vCenter {
    String getLabel() {
        return "vmwareconf"
    }

    String getConfigName() {
        return "vmwareconf.toml"
    }

    AgentCommandConfig convertAll(List<TestServer> servers) {
        // TODO: Create specification
    }

    AgentCommandConfig convert(TestServer server) {
        def config = new AgentCommandConfig(
                server: 'localhost',
                local_exec: false,
                servers : [
                        new ServerConfig(
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
