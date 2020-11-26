package com.getconfig.AgentWrapper


import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import com.getconfig.AgentWrapper.Platform.*

@Slf4j
@CompileStatic
@TypeChecked
@Singleton(strict = false)
class AgentWrapperManager {
    Map<String, AgentConfigWrapper> agentWrappers = new LinkedHashMap<>()

    private AgentWrapperManager() {
        agentWrappers.with {
            it["Linux"] = new Linux()
            it["Windows"] = new Windows()
            it["VMWare"] = new VMWare()
            it["VMHost"] = new VMHost()
            it["CiscoUCS"] = new CiscoUCS()
            it["HPiLO"] = new HPiLO()
            it["Zabbix"] = new Zabbix()
            it["Primergy"] = new Primergy()
            it[AgentConstants.AGENT_LABEL_REMOTE] = new RemoteAgent()
            it[AgentConstants.AGENT_LABEL_LOCAL_FILE] = new LocalAgent()
        }
    }

    AgentConfigWrapper getWrapper(String platform) {
        def agentConfigWrapper = agentWrappers[platform]
        if (!agentConfigWrapper) {
            throw new IllegalArgumentException("not found agent wrapper : " + platform)
        } 
        return agentWrappers[platform] as AgentConfigWrapper
    }
}
