package com.getconfig.Model

import com.getconfig.AgentWrapper.AgentMode
import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString(includePackage = false)
class ServerGroup {
    String groupKey
    AgentMode agentMode
    List<Server> testServers
    String agentLogPath

    ServerGroup(String groupKey, AgentMode agentMode = AgentMode.LocalAgent) {
        this.groupKey = groupKey
        this.agentMode = agentMode
        this.testServers = new ArrayList<Server>()
    }

    void put(Server testServer) {
        this.testServers << testServer
    }

    Server get(int n) {
        return this.testServers[n]
    }

    List<Server> getAll() {
        return this.testServers
    }
}
