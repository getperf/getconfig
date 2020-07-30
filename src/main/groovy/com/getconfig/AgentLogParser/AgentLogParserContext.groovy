package com.getconfig.AgentLogParser

import com.getconfig.AgentLogParser.Platform.*
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
@Singleton(strict = false)
class AgentLogParserContext {
    Map<String, AgentLogParser> agentLogParsers

    private AgentLogParserContext() {
        this.agentLogParsers = new LinkedHashMap<String, AgentLogParser>()
        agentLogParsers.with {
            it["Linux"] = new Linux()
            it["Windows"] = new Windows()
            it["vCenter"] = new vCenter()
            it["VMHost"] = new VMHost()
        }
    }

    AgentLogParser getAgentLogParser(String platform) {
        return this.agentLogParsers.get(platform)
    }
}
