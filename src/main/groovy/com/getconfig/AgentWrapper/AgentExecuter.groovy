package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.TestServer
import com.moandjiezana.toml.TomlWriter
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.apache.commons.validator.routines.UrlValidator

import java.nio.file.Paths

@ToString
@TypeChecked
@CompileStatic
class AgentExecuter implements Controller {
    static final String AgentUrlPrefix = "https://"
    static final int AgentUrlPort = 59001

    String platform
    TestServer server
    AgentConfigWrapper wrapper
    AgentMode agentMode = AgentMode.LocalAgent

    String gconfExe
    String gconfConfigDir
    String currentLogDir
    String tlsConfigDir
    int timeout = 0

    AgentExecuter(String platform, TestServer server) {
        this.platform = platform
        this.server = server
        this.agentMode = this.getAgentMode(platform)
        this.wrapper = ConfigWrapperContext.instance.getWrapper(platform)
    }

    void setEnvironment(ConfigEnv env) {
        this.gconfExe       = env.getGconfExe()
        this.gconfConfigDir = env.getGconfConfigDir()
        this.currentLogDir  = env.getCurrentLogDir()
        this.tlsConfigDir   = env.getTlsConfigDir()
        this.timeout        = env.getGconfTimeout(this.platform)
    }

    AgentMode getAgentMode(String platform) {
        switch(platform){
            case "{Agent}":
                return AgentMode.RemoteAgent
            case "{LocalFile(Hub)}":
                return AgentMode.Hub
            default:
                return AgentMode.LocalAgent
        }
    }

    String toml() {
        AgentCommandConfig config = wrapper.convert(server)
        TomlWriter tomlWriter = new TomlWriter()
        return tomlWriter.write(config)
    }

    String label() {
        return wrapper.getLabel()
    }

    String tomlPath() {
        def label = this.label()
        def serverName = this.server.serverName
        return Paths.get(this.gconfConfigDir, "${label}__${serverName}.toml")
    }

    String tlsPath(String file) {
        return Paths.get(this.tlsConfigDir, file) as String
    }

    String agentUrl() {
        def ip = this.server.ip as String
        if (!ip.startsWith("http")) {
            ip = AgentUrlPrefix + ip + ":" + AgentUrlPort.toString()
        }
        def urlValidator = new UrlValidator()
        if (!urlValidator.isValid(ip)) {
            throw new IllegalArgumentException("url parse error ${ip}")
        }
        return ip
    }

    List<String> args() {
        def args = new ArrayList<String>()
        if (this.agentMode == AgentMode.LocalAgent) {
            args.addAll("-t", this.label())
            args.addAll("-c", this.tomlPath())
            args.addAll("run")
        } else if (this.agentMode == AgentMode.RemoteAgent) {
            args.addAll("get")
            args.addAll("-f", this.agentUrl())
            args.addAll("-ca", tlsPath("ca.cert"))
            args.addAll("-cert", tlsPath("client.pem"))
        }
        args.addAll("-o", this.currentLogDir)
        if (this.timeout > 0) {
            args.addAll("--timeout", this.timeout as String)
        }
        return args
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

