package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.Model.Metric
import com.getconfig.Model.Server
import com.getconfig.Model.ServerGroup
import com.getconfig.Utils.TomlUtils
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.file.Paths

@Slf4j
@ToString
@TypeChecked
@CompileStatic
class LocalAgentBatchExecutor extends LocalAgentExecutor {
    String batchId
    List<Server> testServers
    List<Metric> testMetrics

    LocalAgentBatchExecutor(String batchId, ServerGroup testServers, List<Metric> testMetrics = null) {
        super(testServers.groupKey, testServers.get(0))
        this.batchId = batchId
        this.testServers = testServers.getAll()
        this.testMetrics = testMetrics
    }

    String toml() {
        def config = wrapper.makeAllServersConfig(this.testServers)
        if (!config) {
            throw new IllegalArgumentException("create agent config : not found servers")
        }
        StringBuffer tomlText = new StringBuffer()
        tomlText.append(TomlUtils.decode(config))
        tomlText.append("\n")
        tomlText.append(this.getMetricLibsText())
        return tomlText as String
    }

    String tomlPath() {
        return Paths.get(this.gconfConfigDir, "${this.batchId}.toml")
    }

    List<String> args() {
        def args = new ArrayList<String>()
        args.addAll("-t", this.label())
        args.addAll("-c", this.tomlPath())
        args.addAll("run")
        args.addAll("-o", this.makeAgentLogDir())
        args.addAll("--log-level", AgentConstants.AGENT_LOG_LEVEL as String)
        if (this.level > 0) {
            args.addAll("--level", this.level as String)
        }
        if (this.timeout > 0) {
            args.addAll("--timeout", this.timeout as String)
        }
        return args
    }

    String getAgentLogDir() {
        return Paths.get(this.currentLogDir, this.batchId)
    }

    int run() {
        List<String> serverNames = testServers*.serverName
        String title = "${this.platform} agent(${serverNames})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        def exec = new CommandExec(this.timeout * 1000)
        new File(this.gconfConfigDir).mkdirs()
        new File(this.tomlPath()).write(this.toml(), "UTF-8")
//        new File(this.tomlPath()).with {
//            it.text = this.toml()
//        }
        log.debug "agent command args : ${this.args()}"
        def rc = exec.run(this.gconfExe, this.args() as String[])
        long elapse = System.currentTimeMillis() - start
        log.info "ExitCode : ${rc}, Elapse : ${elapse} ms"
        return rc
    }
}

