package com.getconfig.AgentWrapper

import groovy.transform.*

@CompileStatic
interface AgentConfigWrapper {
    def makeServerConfig(com.getconfig.Model.Server testServer)
    def makeAllServersConfig(List<com.getconfig.Model.Server> servers)
    boolean getBatchEnable()
    String getConfigName()
    String getLabel()
}

