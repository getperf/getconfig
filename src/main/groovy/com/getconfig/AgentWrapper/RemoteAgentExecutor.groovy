package com.getconfig.AgentWrapper

import com.getconfig.CommandExec
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.validator.routines.UrlValidator

import java.nio.file.Paths

@Slf4j
@ToString
@TypeChecked
@CompileStatic
class RemoteAgentExecutor implements AgentExecutor {
    static final String AgentUrlPrefix = "https://"
    static final int AgentUrlPort = 59443

    TestServer server

    String gconfExe
    String currentLogDir
    String tlsConfigDir
    int timeout = 0

    RemoteAgentExecutor(TestServer server) {
        this.server = server
    }

    void setEnvironment(ConfigEnv env) {
        this.gconfExe       = env.getGconfExe()
        this.currentLogDir  = env.getCurrentLogDir()
        this.tlsConfigDir   = env.getTlsConfigDir()
        this.timeout        = env.getGconfTimeout()
    }

    String tlsPath(String file) {
        return Paths.get(this.tlsConfigDir, file) as String
    }

    String agentUrl() {
        // TLS クライアント証明を使用する場合、自己証明書がホスト名で認証されているため、
        // IP アドレスではなく、ホスト名を使用
        // ホスト名からIPをルックアップする機能の追加が必要
        def ip = this.server.remoteAlias ?: this.server.serverName as String
        if (!ip.startsWith("http")) {
            ip = AgentUrlPrefix + ip + ":" + AgentUrlPort.toString()
        }
        def urlValidator = new UrlValidator()
       // if (!urlValidator.isValid(ip)) {
       //     throw new IllegalArgumentException("url parse error ${ip}")
       // }
        return ip
    }

    List<String> args() {
        def args = new ArrayList<String>()
        args.addAll("get")
        args.addAll("-f", this.agentUrl())
        args.addAll("-ca", tlsPath("server/ca.crt"))
        args.addAll("-cert", tlsPath("client.pem"))
        args.addAll("-o", this.makeAgentLogDir())
        if (this.timeout > 0) {
            args.addAll("--timeout", this.timeout as String)
        }
        println args
        return args
    }

    String makeAgentLogDir() {
        String logPath = getAgentLogDir()
        new File(logPath).mkdirs()
        return logPath
    }

    // String makeAgentLogDir(String logPath) {
    //     new File(logPath).mkdirs()
    //     return logPath
    // }

    String getAgentLogDir() {
        return Paths.get(this.currentLogDir, server.serverName)
    }

    // String makeRemoteAgentLogDir() {
    //     String logPath = Paths.get(this.currentLogDir, server.serverName)
    //     return makeAgentLogDir(logPath)
    // }

    int run() {
        String title = "Remote agent executer(${server.serverName})"
        long start = System.currentTimeMillis()
        log.info "Run ${title}"
        def exec = new CommandExec(this.timeout * 1000)
        log.debug "agent command args : ${this.args()}"
        def rc = exec.run(this.gconfExe, this.args() as String[])
        long elapse = System.currentTimeMillis() - start
        log.info "Finish[${rc}],Elapse : ${elapse} ms"
        return rc
    }
}

