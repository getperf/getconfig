package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.AgentLogParser.AgentLogMode
import com.getconfig.AgentLogParser.AgentLogParserManager
import com.getconfig.AgentLogParser.ServerNameAliases
import com.getconfig.Document.ResultGroupManager
import com.getconfig.Model.PlatformParameter
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
    Map<String,PlatformParameter> platformParameters
    public List<AgentLog> agentLogs = new ArrayList<AgentLog>()
    Map<String, ResultGroup> testResultGroups = new LinkedHashMap<>()
    ResultGroupManager resultGroupManager = new ResultGroupManager()
    String filterServer
    String filterMetric

    LogParser(List<Server> testServers,
              Map<String,PlatformParameter> platformParameters = null) {
        this.testServers = testServers
        this.platformParameters = platformParameters
        testServers.each { Server testServer ->
            ResultGroup testResultGroup = new ResultGroup(testServer)
            this.testResultGroups.put(testServer.serverName, testResultGroup)
        }
    }

    @Override
    void setEnvironment(ConfigEnv env) {
        this.filterServer = env.getKeywordServer()
        this.filterMetric = env.getKeywordTest()
        this.agentLogPath = env.getCurrentLogDir()
        this.parserLibPath = env.getAgentLogParserLib()
        env.accept(resultGroupManager)
    }

    void addBatchCommonLog(AgentLog agentLog, List<Server> servers) {
        if (agentLog.metricFile == 'error.log') {
            return
        }
        servers.each { Server server ->
            if (server.domain == agentLog.platform) {
                AgentLog addedAgentLog = new AgentLog(agentLog)
                addedAgentLog.serverName = server.serverName
                this.agentLogs << addedAgentLog
            }
        }
    }

    void makeAgentLogLists() {
        def serverNameAliases = new ServerNameAliases(this.testServers)
        def baseDir = new File(this.agentLogPath)
        baseDir.eachFileRecurse(FileType.FILES) { logFile ->
            def path = logFile.getPath() - baseDir
            AgentLog agentLog = new AgentLog(path, this.agentLogPath).parse()

            // バッチモードでサーバディレクトリがない共通ログの場合、対象ドメインの全サーバログとして登録
            if (agentLog.agentLogMode == AgentLogMode.BATCH &&
                !agentLog.alias) {
                this.addBatchCommonLog(agentLog, this.testServers)
                return
            }
            
            // ログパス名がエイリアスの場合、ServerNameAliases 辞書からサーバ名を取得する
            agentLog.patchServerName(serverNameAliases)
            if (this.filterServer && !(agentLog.serverName=~/${this.filterServer}/) ||
                    (this.filterMetric && !(agentLog.metricFile=~/${this.filterMetric}/))) {
                return
            }
            if (agentLog.agentLogMode != AgentLogMode.UNKNOWN) {
                this.agentLogs << agentLog
            }
        }
    }

    void parseAgentLogs() {
        long start = System.currentTimeMillis()
        AgentLogParserManager parserManager = new AgentLogParserManager(
                this.parserLibPath)
//        parserManager.init()
        long elapse = System.currentTimeMillis() - start
        log.debug "parser load elapse : ${elapse} ms"
        this.agentLogs.each { AgentLog agentLog ->
            parserManager.init(agentLog.platform)
            ResultGroup testResultGroup = this.testResultGroups[agentLog.serverName]
            TestUtil t = new TestUtil(agentLog, testResultGroup,
                    null, platformParameters)
            parserManager.invoke(t)
        }
    }

    void saveTestResultGroups() {
        this.resultGroupManager.saveResultGroups(this.testResultGroups)
    }

    void sumUp() {
        this.testResultGroups.each { String serverName, ResultGroup testResultGroup ->
            testResultGroup.with {
                log.info "finish '${serverName}', ${it.testResults.size()} item / " +
                        "${it.serverPortList.portLists.size()} ip / " +
                        "${it.addedTestMetrics.size()} added metric"
            }
        }
    }

    @Override
    int run() {
        makeAgentLogLists()
        parseAgentLogs()
        sumUp()
        saveTestResultGroups()
        return 0
    }

}
