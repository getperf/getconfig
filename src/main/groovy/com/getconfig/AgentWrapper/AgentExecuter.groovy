package com.getconfig.AgentWrapper

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.ToString
import com.moandjiezana.toml.TomlWriter
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.*

import java.nio.file.Paths

@ToString
@TypeChecked
@CompileStatic
class AgentExecuter implements Controller {
    String platform
    TestServer server
    AgentConfigWrapper wrapper

    String gconfExe
    String gconfConfigDir
    String currentLogDir
    String tlsConfigDir

    AgentExecuter(String platform, TestServer server) {
        this.platform = platform
        this.server = server
        this.wrapper = ConfigWrapperContext.instance.getWrapper(platform)
    }
    void setEnvironment(ConfigEnv env) {
        this.gconfExe       = env.getGconfExe()
        this.gconfConfigDir = env.getGconfConfigDir()
        this.currentLogDir  = env.getCurrentLogDir()
        this.tlsConfigDir   = env.getTlsConfigDir()
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

    List<String> args() {
        def args = new ArrayList<String>()
        args.addAll("-t", this.label())
        args.addAll("-c", this.tomlPath())
        args.addAll("run")
        args.addAll("-o", this.currentLogDir)
        return args
    }
}

