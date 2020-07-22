package com.getconfig

import com.getconfig.AgentWrapper.AgentConfigWrapper
import com.getconfig.AgentWrapper.AgentExecutor
import com.getconfig.AgentWrapper.AgentMode
import com.getconfig.AgentWrapper.LocalAgentExecutor
import com.getconfig.AgentWrapper.LocalAgentBatchExecutor
import com.getconfig.AgentWrapper.RemoteAgentExecutor
import com.getconfig.AgentWrapper.RemoteAgentHubExecutor
import com.getconfig.AgentWrapper.ConfigWrapperContext
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestServerGroup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Collector implements Controller {
    protected List<TestServer> testServers
    private Map<String,TestServerGroup> testServerGroups
    String filterServer

    Collector(List<TestServer> testServers) {
        this.testServers = testServers
        this.testServerGroups = new LinkedHashMap<String,TestServerGroup>()
    }

    void setEnvironment(ConfigEnv env) {
        this.filterServer = env.getKeywordServer()
    }

    AgentMode getAgentMode(String domain) {
        if (domain == '{LocalFile(Hub)}') {
            return AgentMode.RemoteAgentHub
        } else if (domain == '{Agent}') {
            return AgentMode.RemoteAgent
        } else {
            AgentConfigWrapper wrapper = ConfigWrapperContext.instance.getWrapper(domain)
            if (wrapper.getBatchEnable()) {
                return AgentMode.LocalAgentBatch
            } else {
                return AgentMode.LocalAgent
            }
        }
    }

    String getServerGroupKey(AgentMode agentMode, TestServer testServer) {
        String serverGroupKey = "${agentMode}"
        testServer.with {
            if (agentMode == AgentMode.LocalAgentBatch) {
                serverGroupKey += "_${domain}_${ip}_${accountId}"
            } else {
                serverGroupKey += "_${serverName}_${domain}"
            }
        }
        return serverGroupKey
    }

    void classifyTestServers() {
        if (this.filterServer) {
            log.info "set server filter : ${this.filterServer}"
        }
        testServers.each { server ->
            if ((this.filterServer) && !(server.serverName =~ /${this.filterServer}/)) {
                log.info "skip:${server.serverName}, ${server.domain}"
                return
            }
            AgentMode agentMode = this.getAgentMode(server.domain)
            String serverGroupKey = this.getServerGroupKey(agentMode, server)
            if (!testServerGroups.get(serverGroupKey)) {
                testServerGroups[serverGroupKey] = new TestServerGroup(server.domain, agentMode)
            }
            testServerGroups[serverGroupKey].put(server)
        }
    }

    void runAgent() {
        ConfigEnv env = ConfigEnv.instance
        def testServerGroupsSorted = testServerGroups.sort {
            a, b -> a.key <=> b.key
        }
        testServerGroupsSorted.each { key, testServerGroup ->
            def domain = testServerGroup.groupKey
            def agentMode = testServerGroup.agentMode
            def server = testServerGroup.get(0)
            AgentExecutor agentExecutor
            switch(agentMode) {
                case AgentMode.LocalAgent:
                    agentExecutor = new LocalAgentExecutor(domain, server)
                    break
                case AgentMode.LocalAgentBatch:
                    agentExecutor = new LocalAgentBatchExecutor(key, testServerGroup)
                    break
                case AgentMode.RemoteAgent:
                    agentExecutor = new RemoteAgentExecutor(server)
                    break
                case AgentMode.RemoteAgentHub:
                    agentExecutor = new RemoteAgentHubExecutor(testServerGroup)
                    break
            }
            try {
                env.accept(agentExecutor)
                log.info("LOG:${agentExecutor.getAgentLogDir()}")
                agentExecutor.run()
            } catch (IllegalArgumentException e) {
                log.info "${server.serverName} agent error, skip\n $e"
            }
        }
    }

    int run() {
        this.classifyTestServers()
        this.runAgent()
        return 0
    }
}
