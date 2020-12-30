package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Model.PlatformMetric
import com.getconfig.Model.Server
import com.getconfig.Utils.CommonUtil
import com.getconfig.Utils.DirUtils
import com.getconfig.Utils.TomlUtils
import com.moandjiezana.toml.Toml
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.file.Paths

@Slf4j
@ToString
@TypeChecked
@CompileStatic
class DirectExecutor implements AgentExecutor {
    String platform
    Server server
    PlatformMetric platformMetric
    DirectExecutorWrapper wrapper

    String currentLogDir
    String logPath
    String tlsConfigDir
    String metricLib
    String toolsDir
    int timeout = 0
    int level = 0
    boolean dryRun

    DirectExecutor(String platform, Server server) {
        this.platform = platform
        this.server = server
        this.wrapper = AgentWrapperManager.instance.getDirectExecutorWrapper(platform, server.domainExt)
        if (!this.wrapper) {
            throw new IllegalArgumentException("not found agent wrapper : " + platform)
        }
    }

    void setEnvironment(ConfigEnv env) {
        this.currentLogDir = env.getCurrentLogDir()
        this.tlsConfigDir  = env.getTlsConfigDir()
        this.metricLib     = env.getMetricLib()
        this.toolsDir      = env.getGetconfigToolsDir()
        this.level         = env.getLevel()
        this.timeout       = env.getGconfTimeout(this.platform)
        this.dryRun        = env.getDryRun(this.platform)
    }

    void accept(DirectExecutorWrapper visitor) {
        visitor.setEnvironment(this)
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

    void setPlatformMetricFromLibs() {
        def toml = new Toml().read(this.getMetricLibsText())
        this.platformMetric = toml.to(PlatformMetric as Class) as PlatformMetric
        this.platformMetric.validate()
    }

    String makeAgentLogDir() {
        this.logPath = getAgentLogDir()
        if (!dryRun) {
            new File(this.logPath).mkdirs()
        }
        return this.logPath
    }

    String getAgentLogDir() {
        if (server.remoteAlias) {
            return Paths.get(this.currentLogDir, server.serverName, 
                             server.domain, server.remoteAlias)
        } else {
            return Paths.get(this.currentLogDir, server.serverName, 
                             server.domain)
        }
    }

    int run() {
        String title = "${this.platform} agent(${server.serverName})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        setPlatformMetricFromLibs()
        makeAgentLogDir()
        accept(wrapper)
        return (dryRun) ? wrapper.dryRun() : wrapper.run()
    }
}

