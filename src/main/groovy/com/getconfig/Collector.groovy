package com.getconfig

import com.getconfig.AgentWrapper.AgentConfigWrapper
import com.getconfig.AgentWrapper.AgentExecutor
import com.getconfig.AgentWrapper.AgentExecutorBatch
import com.getconfig.AgentWrapper.ConfigWrapperContext
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestServers
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Collector implements Controller {
    protected List<TestServer> testServers
    private Map<String,TestServers> categorizedServers

    Collector(List<TestServer> testServers) {
        this.testServers = testServers
        this.categorizedServers = new LinkedHashMap<String,TestServers>()
    }

    void setEnvironment(ConfigEnv env) {
    }

    String getCategorizedKey(TestServer server) {
        String domain = server.domain
        AgentConfigWrapper wrapper
        wrapper = ConfigWrapperContext.instance.getWrapper(domain)
        String categorizedKey = server.serverName
        if (wrapper.getBatchEnable()) {
            server.with {
                categorizedKey = (ip) ? "${domain}_${ip}_${accountId}" :
                        "${domain}_${accountId}"
            }
        }
        return categorizedKey
    }

    void classifyTestServers() {
        ConfigEnv env = ConfigEnv.instance
        String filterServer = env.getKeywordServer()
        if (filterServer) {
            log.info "set server filter : ${filterServer}"
        }
        testServers.each { server ->
            if ((filterServer) && !(server.serverName =~ /${filterServer}/)) {
                log.info "skip:${server.serverName}"
                return
            }
            String categorizedKey = this.getCategorizedKey(server)
            if (!categorizedServers.get(categorizedKey)) {
                categorizedServers[categorizedKey] = new TestServers(server.domain)
            }
            categorizedServers[categorizedKey].put(server)
        }
    }

    void runAgent() {
        ConfigEnv env = ConfigEnv.instance
        categorizedServers.each { categorizedKey, testServers ->
            def domain = testServers.domain
            if  (ConfigWrapperContext.instance.getWrapper(domain).getBatchEnable()) {
                log.info("run batch ${categorizedKey}")
                try {
                    def agentExecutorBatch = new AgentExecutorBatch(categorizedKey, testServers)
                    env.accept(agentExecutorBatch)
                    agentExecutorBatch.run()
                } catch (IllegalArgumentException e) {
                    log.info "batch ${categorizedKey} error, skip\n $e"
                }
            } else {
                TestServer server = testServers.get(0)
                log.info("run ${server.serverName}")
                try {
                    def agentExecutor = new AgentExecutor(domain, server)
                    env.accept(agentExecutor)
                    agentExecutor.run()
                } catch (IllegalArgumentException e) {
                    log.info "${server.serverName} agent error, skip\n $e"
                }
            }
        }
    }

    void run() {
        this.classifyTestServers()
        this.runAgent()
    }
}
