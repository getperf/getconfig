package com.getconfig.AgentWrapper.Platform

import com.getconfig.AgentWrapper.AgentConfigWrapper
import com.getconfig.Model.Server

// import groovy.util.logging.Slf4j

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

// @Slf4j
@CompileStatic
@InheritConstructors
class NetApp implements AgentConfigWrapper {
    class NetAppConfig {
        String server
        String url
        String user
        String password

        List<String> servers = new ArrayList<String>()
    }

    @Override
    String getLabel() {
        return "netappconf"
    }

    @Override
    String getConfigName() {
        return "netappconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return true
    }

    @Override
    def makeAllServersConfig(List<Server> servers) {
        def firstServer = servers[0]
        String ip = firstServer.ip
        def config = new NetAppConfig(
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
    def makeServerConfig(Server server) {
        String ip = server.ip
        def remoteAlias = server.remoteAlias ?: server.serverName
        def config = new NetAppConfig(
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
