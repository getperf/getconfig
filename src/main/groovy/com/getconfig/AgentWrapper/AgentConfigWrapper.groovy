package com.getconfig.AgentWrapper

import groovy.transform.*
import com.getconfig.Model.*

@CompileStatic
interface AgentConfigWrapper {
    def makeServerConfig(TestServer testServer)
    def makeAllServersConfig(List<TestServer> servers)
    boolean getBatchEnable()
    String getConfigName()
    String getLabel()
}

