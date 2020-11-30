package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

@Slf4j
@InheritConstructors
class CiscoUCS implements AgentConfigWrapper {
    class ServerModel {
        String  server
        String  url
        String  user
        String  password
        String  ssh_key
        boolean insecure
    }

    class CiscoUCSConfig {
        String  server
        String  url
        String  user
        String  password
        String  ssh_key
        boolean insecure

        List<ServerModel> servers = new ArrayList<>()
    }

    @Override
    String getLabel() {
        return "ciscoucsconf"
    }

    @Override
    String getConfigName() {
        return "ciscoucs.toml"
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
        def config = new CiscoUCSConfig(
                server : server.serverName,
                url : server.ip,
                user : server.user,
                password : server.password,
                ssh_key : server.loginOption,
                insecure : true,
                // servers: [
                //         new ServerModel(
                //                 server : server.serverName,
                //                 url : server.ip,
                //                 user : server.user,
                //                 password : server.password,
                //                 ssh_key : server.loginOption,
                //                 insecure : true,
                //         ),
                // ],
        )
        return config
    }
}
