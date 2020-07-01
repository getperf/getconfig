package com.getconfig.AgentWrapper

import groovy.transform.*
import com.getconfig.Model.*

@CompileStatic
interface AgentConfigWrapper {
    AgentCommandConfig convertAll(List<TestServer> servers)
    AgentCommandConfig convert(TestServer server)
    String getConfigName()
    String getLabel()
}

