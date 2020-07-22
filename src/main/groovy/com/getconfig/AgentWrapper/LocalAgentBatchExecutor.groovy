package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestServerGroup
import com.moandjiezana.toml.TomlWriter
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
    List<TestServer> testServers

    LocalAgentBatchExecutor(String batchId, TestServerGroup testServers) {
        super(testServers.groupKey, testServers.get(0))
        this.batchId = batchId
        this.testServers = testServers.getAll()
    }

    String toml() {
        def config = wrapper.makeAllServersConfig(this.testServers)
        if (!config) {
            throw new IllegalArgumentException("create agent config : not found servers")
        }
        TomlWriter tomlWriter = new TomlWriter()
       return tomlWriter.write(config)
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
        String title = "${this.platform} agent executer(${serverNames})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        def exec = new CommandExec(this.timeout * 1000)
        new File(this.gconfConfigDir).mkdirs()
        new File(this.tomlPath()).with {
            it.text = this.toml()
        }
        log.debug "agent command args : ${this.args()}"
        def rc = exec.run(this.gconfExe, this.args() as String[])
        long elapse = System.currentTimeMillis() - start
        log.info "Finish[${rc}],Elapse : ${elapse} ms"
        return rc
    }
}

