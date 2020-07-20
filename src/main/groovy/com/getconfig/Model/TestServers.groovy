package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString
class TestServers {
    String domain
    List<TestServer> testServers

    TestServers(String domain) {
        this.domain = domain
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
