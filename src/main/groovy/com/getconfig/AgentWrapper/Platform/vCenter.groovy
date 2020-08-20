package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
// import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestMetric

// @Slf4j
@CompileStatic
@InheritConstructors
class vCenter implements AgentConfigWrapper {
    class vCenterConfig {
        String server
        String url
        String user
        String password
        boolean local_exec = false

        List<String> servers = new ArrayList<String>()
    }

    @Override
    String getLabel() {
        return "vmwareconf"
    }

    @Override
    String getConfigName() {
        return "vmwareconf.toml"
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
        def config = new vCenterConfig(
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
        def config = new vCenterConfig(
                server: server.serverName,
                url: ip,
                user: server.user,
                password: server.password,

                servers : [
                        remoteAlias
                ]
        )
        return config
    }
}
