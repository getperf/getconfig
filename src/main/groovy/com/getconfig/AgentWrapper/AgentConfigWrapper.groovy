package com.getconfig.AgentWrapper

import groovy.transform.*
import com.getconfig.Model.*

@CompileStatic
interface AgentConfigWrapper {
    def makeAllServersConfig(List<TestServer> servers)
    def makeServerConfig(TestServer server)
    boolean getBatchEnable()
    String getConfigName()
    String getLabel()
}

