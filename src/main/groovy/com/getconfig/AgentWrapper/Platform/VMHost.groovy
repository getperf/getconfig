package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic
@InheritConstructors
class VMHost implements AgentConfigWrapper {
    class VMHostConfig {
        String server
        String url
        String user
        String password
        boolean local_exec = false

        List<String> servers = new ArrayList<String>()
    }

    @Override
    String getLabel() {
        return "vmhostconf"
    }

    @Override
    String getConfigName() {
        return "vmhostconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return true
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers) {
        def firstServer = servers[0]
        String ip = firstServer.ip
        if (!ip.startsWith("http")) {
            ip = "https://${ip}/sdk"
        }
        def config = new VMHostConfig(
                server: "",
                url: ip,
                user: firstServer.user,
                password: firstServer.password
        )
        servers.each { server ->
            String remoteAlias = server.remoteAlias ?: server.serverName
            config.servers << remoteAlias
        }
        return config
    }

    @Override
    def makeServerConfig(TestServer server) {
        String ip = server.ip
        if (!ip.startsWith("http")) {
            ip = "https://${ip}/sdk"
        }
        def remoteAlias = server.remoteAlias ?: server.serverName
        def config = new VMHostConfig(
                server: remoteAlias,
                url: ip,
                user: server.user,
                password: server.password,
        )
        return config
    }
}
