package com.getconfig.AgentLogParser

import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class ServerNameAliases {
    Map<String, String> serverNameAliases

    ServerNameAliases(List<TestServer> testServers) {
        this.serverNameAliases = new LinkedHashMap<String, String>()
        testServers.each { testServer ->
            String alias = testServer.remoteAlias ?: testServer.serverName
            if (!this.serverNameAliases.containsKey(alias)) {
                this.serverNameAliases.put(alias, testServer.serverName)
            }
        }
    }

    String getServerName(String alias) {
        return serverNameAliases.get(alias)
    }
}
