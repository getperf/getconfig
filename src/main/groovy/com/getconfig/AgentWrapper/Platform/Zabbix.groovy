package com.getconfig.AgentWrapper.Platform

import com.getconfig.AgentWrapper.AgentConfigWrapper
import com.getconfig.Model.Server

// import groovy.util.logging.Slf4j

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

// @Slf4j
@CompileStatic
@InheritConstructors
class Zabbix implements AgentConfigWrapper {
    class ZabbixConfig {
        String server
        String url
        String user
        String password

        List<String> servers = new ArrayList<String>()
    }

    @Override
    String getLabel() {
        return "zabbixconf"
    }

    @Override
    String getConfigName() {
        return "zabbixconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return true
    }

    @Override
    def makeAllServersConfig(List<Server> servers) {
        def firstServer = servers[0]
        String ip = firstServer.ip
        if (!ip.startsWith("http")) {
            ip = "http://${ip}/zabbix"
        }
        def config = new ZabbixConfig(
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
        if (!ip.startsWith("http")) {
            ip = "http://${ip}/zabbix"
        }
        def remoteAlias = server.remoteAlias ?: server.serverName
        def config = new ZabbixConfig(
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
