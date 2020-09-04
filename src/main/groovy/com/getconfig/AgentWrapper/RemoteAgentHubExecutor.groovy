package com.getconfig.AgentWrapper


import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestServerGroup
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import java.nio.file.Paths

@Slf4j
@ToString
@TypeChecked
@CompileStatic
class RemoteAgentHubExecutor implements AgentExecutor {
    List<TestServer> testServers
    String currentLogDir
    String hubInventoryDir

    RemoteAgentHubExecutor(TestServerGroup testServerGroup) {
        this.testServers = testServers = testServerGroup.getAll()
    }

    void setEnvironment(ConfigEnv env) {
        this.currentLogDir = env.getCurrentLogDir()
        this.hubInventoryDir = env.getHubInventoryDir()
    }

    String getAgentLogDir() {
        def server = this.testServers.get(0)
        return Paths.get(this.currentLogDir, server.serverName)
    }

    int run() {
        int rc = 0
        List<String> serverNames = testServers*.serverName
        String title = "Remote agent(HUB) executer(${serverNames})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        testServers.each { testServer ->
            String serverName = testServer.serverName
            log.info("copy local log file from hub directory : ${serverName}")
            try {
                def sourceDir = new File(this.hubInventoryDir, serverName)
                def targetDir = new File(this.currentLogDir, serverName)
                FileUtils.copyDirectory(sourceDir, targetDir)
            } catch (IOException e) {
                rc = 1
                log.error("copy hub inventory dir : ${e}")
            }
        }
        long elapse = System.currentTimeMillis() - start
        log.info "ExitCode : ${rc}, Elapse : ${elapse} ms"
        return rc
    }
}

