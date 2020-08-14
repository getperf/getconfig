package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.AgentLogParser.AgentLogMode
import com.getconfig.AgentLogParser.AgentLogParserManager
import com.getconfig.AgentLogParser.ServerNameAliases
import com.getconfig.Model.TestResultGroup
import com.getconfig.Model.TestServer
import com.getconfig.Testing.TestUtil
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class LogParser implements Controller {
    String agentLogPath
    String parserLibPath
    protected List<TestServer> testServers
    protected List<AgentLog> agentLogs = new ArrayList<AgentLog>()
    Map<String, TestResultGroup> testResultGroups = new LinkedHashMap<>()

    LogParser(List<TestServer> testServers) {
        this.testServers = testServers
        testServers.each {TestServer testServer ->
            TestResultGroup testResultGroup = new TestResultGroup(testServer)
            this.testResultGroups.put(testServer.serverName, testResultGroup)
        }
    }

    @Override
    void setEnvironment(ConfigEnv env) {
        this.agentLogPath = env.getCurrentLogDir()
        this.parserLibPath = env.getAgentLogParserLib()
    }

    void makeAgentLogs() {
        def serverNameAliases = new ServerNameAliases(this.testServers)
        def baseDir = new File(this.agentLogPath)
        baseDir.eachFileRecurse(FileType.FILES) { logFile ->
            def path = logFile.getPath() - baseDir
            AgentLog agentLog = new AgentLog(path, this.agentLogPath).parse()
            agentLog.patchServerName(serverNameAliases)
            if (agentLog.agentLogMode != AgentLogMode.UNKNOWN) {
                this.agentLogs << agentLog
            }
        }
    }

    void parseAgentLogs() {
        println this.parserLibPath
        AgentLogParserManager parserManager = new AgentLogParserManager(this.parserLibPath)
        parserManager.init()
        this.agentLogs.each { AgentLog agentLog ->
            TestUtil t = new TestUtil(agentLog)
            parserManager.invoke(t)
        }
    }

    @Override
    int run() {
        makeAgentLogs()
        parseAgentLogs()
        return 0
    }
}
