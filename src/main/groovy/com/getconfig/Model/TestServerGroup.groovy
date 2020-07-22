package com.getconfig.Model

import com.getconfig.AgentWrapper.AgentMode
import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString
class TestServerGroup {
    String groupKey
    AgentMode agentMode
    List<TestServer> testServers

    TestServerGroup(String groupKey, AgentMode agentMode) {
        this.groupKey = groupKey
        this.agentMode = agentMode
        this.testServers = new ArrayList<TestServer>()
    }

    void put(TestServer testServer) {
        this.testServers << testServer
    }

    TestServer get(int n) {
        return this.testServers[n]
    }

    List<TestServer> getAll() {
        return this.testServers
    }
}
