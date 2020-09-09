package com.getconfig.AgentLogParser

import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class ServerNameAliases {
    Map<String, String> serverNameAliases

    ServerNameAliases(List<Server> testServers) {
        this.serverNameAliases = new LinkedHashMap<String, String>()
        testServers.each { testServer ->
            String alias = testServer.remoteAlias ?: testServer.serverName
            // エイリアスとサーバ名の両方を辞書に登録
            [alias, testServer.serverName].each { key ->
                if (!this.serverNameAliases.containsKey(key)) {
                    this.serverNameAliases.put(key, testServer.serverName)
                }
            }
        }
    }

    String getServerName(String alias) {
        return serverNameAliases.get(alias)
    }
}
