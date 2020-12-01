package com.getconfig.AgentWrapper.Platform

import com.getconfig.Model.PlatformMetric
import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

@Slf4j
@InheritConstructors
class Oracle implements AgentConfigWrapper {
    class ServerModel {
        String  server
        String  url
        String  user
        String  password
        String  ssh_key
        boolean insecure
    }

    class OracleConfig {
        String  server
        String  url
        String  user
        String  password
        String  ssh_key
        boolean insecure
        List<ServerModel> servers = new ArrayList<>()
        PlatformMetric metrics
    }

    @Override
    String getLabel() {
        return "oracleconf"
    }

    @Override
    String getConfigName() {
        return "oracleconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return false
    }

    @Override
    def makeAllServersConfig(List<Server> servers) {
        // TODO: Create specification
    }

    @Override
    def makeServerConfig(Server server) {
        def config = new OracleConfig(
            server : server.serverName,
            url : server.ip,
            user : server.user,
            password : server.password,
            ssh_key : server.loginOption,
            insecure : true,
        )
        return config
    }
}
