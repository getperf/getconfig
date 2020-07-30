package com.getconfig.AgentLogParser

import spock.lang.Specification

class AgentLogTest extends Specification {
    def "ログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        "\\centos80\\Linux\\centos80\\cpu" | "Linux"  | AgentLogMode.NORMAL
        "\\centos80\\Linux\\error.log"     | null     | AgentLogMode.UNKNOWN
    }

    def "リモートエージェントログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        "\\server01\\Linux\\20200722\\103000\\loadavg.txt" | null | AgentLogMode.UNKNOWN
        "\\server01\\LinuxConf\\20200722\\103500\\centos80\\cpu" | "Linux" | AgentLogMode.NORMAL
        "\\server01\\LinuxConf\\20200722\\103500\\error.log" | null | AgentLogMode.UNKNOWN
        "\\server01\\VMHostConf\\20200722\\103500\\esxi.ostrich\\all.json" | "VMHost" | AgentLogMode.NORMAL
    }

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

    def "Hub サーバログパス解析"(String path, String platform, AgentLogMode agentLogMode) {
        expect:
        def agentLog = new AgentLog(path).parse()
        agentLog.agentLogMode == agentLogMode
        agentLog.platform == platform

        where:
        path                               | platform | agentLogMode
        "\\server02\\WindowsConf\\cpu" | "Windows" | AgentLogMode.NORMAL
    }
}
