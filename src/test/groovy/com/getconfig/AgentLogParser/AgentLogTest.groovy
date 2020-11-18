package com.getconfig.AgentLogParser

import com.getconfig.AgentLogParser.ServerNameAliases
import com.getconfig.Model.Server
import com.getconfig.TestData
import spock.lang.IgnoreIf
import spock.lang.Specification

class AgentLogTest extends Specification {
    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "ログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform
        def agentLog2 = new AgentLog("\\centos80\\VMWare\\centos80\\cpu").parse()
        println agentLog2

        where:
        path                               | platform | agentLogMode
        "\\centos80\\Linux\\centos80\\cpu" | "Linux"  | AgentLogMode.NORMAL
        "\\centos80\\Linux\\error.log"     | null     | AgentLogMode.UNKNOWN
    }

    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "リモートエージェントログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        //noinspection SpellCheckingInspection
        "\\server01\\Linux\\20200722\\103000\\loadavg.txt" | null | AgentLogMode.UNKNOWN
        "\\server01\\LinuxConf\\20200722\\103500\\centos80\\cpu" | "Linux" | AgentLogMode.NORMAL
        "\\server01\\LinuxConf\\20200722\\103500\\error.log" | null | AgentLogMode.UNKNOWN
        "\\server01\\VMHostConf\\20200722\\103500\\esxi.ostrich\\all.json" | "VMHost" | AgentLogMode.NORMAL
    }

    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "ローカルエージェントバッチログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        "\\LocalAgentBatch_vCenter_192.168.10.100_Account01\\centos80\\all.json" | "vCenter" | AgentLogMode.BATCH
        "\\LocalAgentBatch_vCenter_192.168.10.100_Account01\\error.log" | null | AgentLogMode.UNKNOWN
    }

    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "Hub サーバログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        "\\server02\\WindowsConf\\cpu" | "Windows" | AgentLogMode.NORMAL
    }

    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "DryRunログパス解析"() {
        when:
        def agentLog = new AgentLog("\\centos80\\VMWare\\centos80\\cpu").parse()

        then:
        agentLog.serverName == 'centos80'
        agentLog.platform == 'VMWare'
    }

    @IgnoreIf({ !System.getProperty("os.name").contains("windows") })
    def "DryRunバッチログパス解析"(String inPath, String outPath) {
        expect:
        List<Server> testServers = TestData.readTestServers()
        def serverNameAliases = new ServerNameAliases(testServers)
        def agentLog = new AgentLog(inPath).parse()
        agentLog.patchServerName(serverNameAliases)
        agentLog.getProjectLogPath() == outPath

        where:
        inPath                               | outPath
        "LocalAgentBatch_VMWare_192.168.10.100_Account01\\centos80\\all.json" | "centos80\\VMWare\\centos80\\all.json"
        "\\centos80\\Linux\\centos80\\cpu" | "centos80\\Linux\\centos80\\cpu"
    }

}
