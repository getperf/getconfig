package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestServers
import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked

import java.nio.file.Paths

@ToString
@TypeChecked
@CompileStatic
class AgentExecutorBatch extends AgentExecutor {
    String batchId
    List<TestServer> servers

    AgentExecutorBatch(String batchId, TestServers testServers) {
        super(testServers.domain, testServers.get(0))
        this.batchId = batchId
        this.servers = testServers.getAll()
    }

    String toml() {
        def config = wrapper.makeAllServersConfig(this.servers)
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
        if (this.agentMode == AgentMode.LocalAgent) {
            args.addAll("-t", this.label())
            args.addAll("-c", this.tomlPath())
            args.addAll("run")
            args.addAll("-o", this.makeLocalAgentLogDir())
        }
        if (this.timeout > 0) {
            args.addAll("--timeout", this.timeout as String)
        }
        return args
    }

    String makeLocalAgentLogDir() {
        String logPath = Paths.get(this.currentLogDir, this.batchId)
        return makeAgentLogDir(logPath)
    }

    int run() {
        def exec = new CommandExec(this.timeout * 1000)
        new File(this.gconfConfigDir).mkdirs()
        new File(this.tomlPath()).with {
            it.text = this.toml()
        }
        return exec.run(this.gconfExe, this.args() as String[])
    }
}

