package com.getconfig

import com.getconfig.AgentWrapper.AgentExecuter
import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Collector implements Controller {
    protected List<TestServer> testServers

    Collector(List<TestServer> testServers) {
        this.testServers = testServers
    }

    void setEnvironment(ConfigEnv env) {
    }

    void run() {
        def env = ConfigEnv.instance
        testServers.each { server ->
            try {
                def domain = server.domain
                def agentExecuter = new AgentExecuter(domain, server)
                env.accept(agentExecuter)
                agentExecuter.run()
//                println agentExecuter.args()
            } catch (IllegalArgumentException e) {
                log.info "create ${server.serverName} config error, skip\n $e"
            }
        }
    }
}
