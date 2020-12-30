package com.getconfig.AgentWrapper.Platform

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Utils.CommonUtil
import groovy.transform.*
import groovy.util.logging.Slf4j
import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
@InheritConstructors
class WindowsPy implements DirectExecutorWrapper {
    Server server
    String gconfExe
    String logPath
    int timeout = 0
    int level = 0

    @Override
    String getLabel() {
        return "windowsconf"
    }

    @Override
    void setEnvironment(DirectExecutor executor) {
        this.logPath = executor.logPath
        this.timeout = executor.timeout
        this.level = executor.level
        this.server = executor.server
        String gconfExeName = CommonUtil.isWindows() ? "gcwinrm.bat" : "gcwinrm"
        this.gconfExe = Paths.get(executor.toolsDir, gconfExeName)
    }

    List<String> args(LocalAgentExecutor executor) {
        def args = new ArrayList<String>()
        args.addAll("-c", executor.tomlPath())
        args.addAll("-o", executor.makeAgentLogDir())
        if (this.level > 0) {
            args.addAll("--level", this.level as String)
        }
        if (this.timeout > 0) {
            args.addAll("--timeout", this.timeout as String)
        }
        return args
    }

    @Override
    int run() {
        log.info("run Python Windows direct collector : ${this.server.serverName}")
        long start = System.currentTimeMillis()
        LocalAgentExecutor executor
        executor = new LocalAgentExecutor("Windows", this.server)
        ConfigEnv.instance.accept(executor)
        new File(executor.gconfConfigDir).mkdirs()
        new File(executor.tomlPath()).write(executor.toml(), "UTF-8")

        println "COMMAND: ${this.args(executor)}"
        log.debug "agent command args : ${this.args(executor)}"
        def exec = new CommandExec(this.timeout * 1000)
        def rc = exec.run(this.gconfExe, this.args(executor) as String[])
        long elapse = System.currentTimeMillis() - start
        log.info "ExitCode : ${rc}, Elapse : ${elapse} ms"
        return rc
    }

    int dryRun() {
        log.info("run Python Windows direct collector : ${this.server.serverName}")
        log.info("logpath ${this.logPath}")
        return 0
    }
}
