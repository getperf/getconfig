package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.AgentLogParser.AgentLogMode
import com.getconfig.AgentLogParser.AgentLogParser
import com.getconfig.AgentLogParser.AgentLogParserContext
import com.getconfig.AgentLogParser.ServerNameAliases
import com.getconfig.Model.TestServer
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.file.Paths

@Slf4j
@TypeChecked
@CompileStatic
class LogParser implements Controller {
    String baseDir
    protected List<TestServer> testServers
    protected List<AgentLog> agentLogs = new ArrayList<AgentLog>()

    LogParser(List<TestServer> testServers) {
        this.testServers = testServers
    }

    @Override
    void setEnvironment(ConfigEnv env) {
        this.baseDir = env.getCurrentLogDir()
    }

    void makeAgentLogs() {
        def serverNameAliases = new ServerNameAliases(this.testServers)
        def baseDir = new File(this.baseDir)
        baseDir.eachFileRecurse(FileType.FILES) { logFile ->
            def agentLogPath = logFile.getPath() - baseDir
            AgentLog agentLog = new AgentLog(agentLogPath).parse()
            agentLog.patchServerName(serverNameAliases)
            if (agentLog.agentLogMode != AgentLogMode.UNKNOWN) {
                this.agentLogs << agentLog
            }
        }
    }

    void parseAgentLogs() {
        this.agentLogs.each { AgentLog agentLog ->
            String methodName = CommonUtil.toCamelCase(agentLog.metricFile)
            // println "${agentLog.serverName}, ${agentLog.platform}, ${methodName}"
            String platform = agentLog.platform
            AgentLogParser agentLogParser = AgentLogParserContext.instance.getAgentLogParser(platform)
            if (agentLogParser) {
                def method = agentLogParser.metaClass.getMetaMethod(methodName, Reader)
                if (method) {
                    println "METHOD : ${agentLog.serverName}, ${agentLog.platform}, ${methodName}"
                    def inFile = Paths.get(this.baseDir, agentLog.path).toFile()
                    println method.invoke(agentLogParser, inFile.newReader())
                }
            }
        }
    }

    @Override
    int run() {
        makeAgentLogs()
        parseAgentLogs()
        return 0
    }
}
