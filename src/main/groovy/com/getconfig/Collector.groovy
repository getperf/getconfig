package com.getconfig

import com.getconfig.AgentWrapper.AgentConfigWrapper
import com.getconfig.AgentWrapper.AgentExecutor
import com.getconfig.AgentWrapper.AgentMode
import com.getconfig.AgentWrapper.LogManager
import com.getconfig.AgentWrapper.LocalAgentExecutor
import com.getconfig.AgentWrapper.LocalAgentBatchExecutor
import com.getconfig.AgentWrapper.RemoteAgentExecutor
import com.getconfig.AgentWrapper.RemoteAgentHubExecutor
import com.getconfig.AgentWrapper.AgentWrapperManager
import com.getconfig.Model.Server
import com.getconfig.Model.ServerGroup
import com.getconfig.Utils.CommonUtil
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Collector implements Controller {
    protected List<Server> testServers
    private Map<String,ServerGroup> testServerGroups
    boolean dryRun
    String projectLogDir
    String currentLogDir
    String filterServer

    Collector(List<Server> testServers) {
        this.testServers = testServers
        this.testServerGroups = new LinkedHashMap<String,ServerGroup>()
    }

    void setEnvironment(ConfigEnv env) {
        this.dryRun = env.getDryRun()
        this.filterServer = env.getKeywordServer()
        this.projectLogDir = env.getProjectLogDir()
        this.currentLogDir = env.getCurrentLogDir()
    }

    Map<String, ServerGroup> getTestServerGroups() {
        return testServerGroups
    }

    AgentMode getAgentMode(String domain) {
        if (domain == '{LocalFile}') {
            return AgentMode.RemoteAgentHub
        } else if (domain == '{Agent}') {
            return AgentMode.RemoteAgent
        } else {
            AgentConfigWrapper wrapper = AgentWrapperManager.instance.getWrapper(domain)
            if (wrapper.getBatchEnable()) {
                return AgentMode.LocalAgentBatch
            } else {
                return AgentMode.LocalAgent
            }
        }
    }

    String getServerGroupKey(AgentMode agentMode, Server testServer) {
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
                testServerGroups[serverGroupKey] = new ServerGroup(server.domain, agentMode)
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
                testServerGroup.agentLogPath = agentExecutor.getAgentLogDir()
                if (dryRun) {
                    LogManager.restoreProjectLogs(this, agentExecutor.getAgentLogDir())
                }else {
                    agentExecutor.run()
                }
            } catch (IllegalArgumentException e) {
                log.info "${server.serverName} agent error, skip\n $e"
            }
        }
    }

    int run() {
        CommonUtil.resetDir(this.currentLogDir)
        this.classifyTestServers()
        this.runAgent()
        return 0
    }

}
