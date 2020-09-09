package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Model.Server
import com.getconfig.Utils.DirUtils
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
class LocalAgentExecutor implements AgentExecutor {
    String platform
    Server server
    AgentConfigWrapper wrapper

    String gconfExe
    String gconfConfigDir
    String currentLogDir
    String tlsConfigDir
    String metricLib
    int timeout = 0
    int level = 0

    LocalAgentExecutor(String platform, Server server) {
        this.platform = platform
        this.server = server
        this.wrapper = AgentWrapperManager.instance.getWrapper(platform)
    }

    void setEnvironment(ConfigEnv env) {
        this.gconfExe       = env.getGconfExe()
        this.gconfConfigDir = env.getAgentConfigDir()
        this.currentLogDir  = env.getCurrentLogDir()
        this.tlsConfigDir   = env.getTlsConfigDir()
        this.metricLib      = env.getMetricLib()
        this.level          = env.getLevel()
        this.timeout        = env.getGconfTimeout(this.platform)
    }

    String toml() {
        def config = wrapper.makeServerConfig(this.server)
        StringBuffer tomlText = new StringBuffer()
        tomlText.append(TomlUtils.decode(config))
        tomlText.append("\n")
        tomlText.append(this.getMetricLibsText())
        return tomlText as String
    }

    String label() {
        return wrapper.getLabel()
    }

    String getMetricLibsText() {
        def metricFiles = DirUtils.ls(this.metricLib,
                /^${this.platform}.toml$/, /^${this.platform}__(.+).toml$/)
        StringBuffer metricText = new StringBuffer()
        metricFiles.each {metricFile ->
            metricText.append(metricFile.text)
        }
        return metricText as String
    }

    String tomlPath() {
        def label = this.label()
        def serverName = this.server.serverName
        return Paths.get(this.gconfConfigDir, "${label}__${serverName}.toml")
    }

    String tlsPath(String file) {
        return Paths.get(this.tlsConfigDir, file) as String
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

    String makeAgentLogDir() {
        String logPath = getAgentLogDir()
        new File(logPath).mkdirs()
        return logPath
    }

    String getAgentLogDir() {
        return Paths.get(this.currentLogDir, server.serverName, server.domain)
    }

    // String makeLocalAgentLogDir() {
    //     String logPath = Paths.get(this.currentLogDir, server.serverName, server.domain)
    //     return makeAgentLogDir(logPath)
    // }

    int run() {
        String title = "${this.platform} agent(${server.serverName})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        def exec = new CommandExec(this.timeout * 1000)
        new File(this.gconfConfigDir).mkdirs()
        new File(this.tomlPath()).write(this.toml(), "UTF-8")

        log.debug "agent command args : ${this.args()}"
        def rc = exec.run(this.gconfExe, this.args() as String[])
        long elapse = System.currentTimeMillis() - start
        log.info "ExitCode : ${rc}, Elapse : ${elapse} ms"
        return rc
    }
}

