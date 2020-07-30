package com.getconfig.AgentWrapper

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import com.getconfig.AgentWrapper.Platform.*

@Slf4j
@CompileStatic
@TypeChecked
@Singleton(strict = false)
class AgentWrapperContext {
    def agentWrappers = new LinkedHashMap<String, AgentConfigWrapper>()

    private AgentWrapperContext() {
        agentWrappers.with {
            it["Linux"] = new Linux()
            it["Windows"] = new Windows()
            it["vCenter"] = new vCenter()
            it["VMHost"] = new VMHost()
            it["{Agent}"] = new RemoteAgent()
            it["{LocalFile(Hub)}"] = new LocalAgent()
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
