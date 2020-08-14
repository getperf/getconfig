package com.getconfig.AgentWrapper

import groovy.transform.*
import com.getconfig.Model.*

@CompileStatic
interface AgentConfigWrapper {
    def makeServerConfig(TestServer testServer, List<TestMetric> testMetrics)
    def makeAllServersConfig(List<TestServer> servers, List<TestMetric> testMetrics)
    boolean getBatchEnable()
    String getConfigName()
    String getLabel()
}

