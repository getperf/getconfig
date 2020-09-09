package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.AgentLogParser.AgentLogMode
import com.getconfig.AgentLogParser.AgentLogParserManager
import com.getconfig.AgentLogParser.ServerNameAliases
import com.getconfig.Model.ResultGroup
import com.getconfig.Model.Server
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
    protected List<Server> testServers
    protected List<AgentLog> agentLogs = new ArrayList<AgentLog>()
    Map<String, ResultGroup> testResultGroups = new LinkedHashMap<>()

    LogParser(List<Server> testServers) {
        this.testServers = testServers
        testServers.each { Server testServer ->
            ResultGroup testResultGroup = new ResultGroup(testServer)
            this.testResultGroups.put(testServer.serverName, testResultGroup)
        }
    }

    @Override
    void setEnvironment(ConfigEnv env) {
        this.agentLogPath = env.getCurrentLogDir()
        this.parserLibPath = env.getAgentLogParserLib()
    }

    void MakeAgentLogLists() {
        def serverNameAliases = new ServerNameAliases(this.testServers)
        def baseDir = new File(this.agentLogPath)
        baseDir.eachFileRecurse(FileType.FILES) { logFile ->
            def path = logFile.getPath() - baseDir
            AgentLog agentLog = new AgentLog(path, this.agentLogPath).parse()
            // ログパス名がエイリアスの場合、ServerNameAliases 辞書からサーバ名を取得する
            agentLog.patchServerName(serverNameAliases)
            if (agentLog.agentLogMode != AgentLogMode.UNKNOWN) {
                this.agentLogs << agentLog
            }
        }
    }

    void parseAgentLogs() {
        long start = System.currentTimeMillis()
        AgentLogParserManager parserManager = new AgentLogParserManager(this.parserLibPath)
//        parserManager.init()
        long elapse = System.currentTimeMillis() - start
        log.debug "parser load elapse : ${elapse} ms"
        this.agentLogs.each { AgentLog agentLog ->
            parserManager.init(agentLog.platform)
            ResultGroup testResultGroup = this.testResultGroups[agentLog.serverName]
            TestUtil t = new TestUtil(agentLog, testResultGroup)
            parserManager.invoke(t)
        }
    }

    @Override
    int run() {
        MakeAgentLogLists()
        parseAgentLogs()
        return 0
    }
}
