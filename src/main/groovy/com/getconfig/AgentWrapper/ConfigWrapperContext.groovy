package com.getconfig.AgentWrapper

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import com.getconfig.AgentWrapper.Platform.*

@Slf4j
@CompileStatic
@TypeChecked
@Singleton(strict = false)
class ConfigWrapperContext {
    def agentConfigWrappers = new LinkedHashMap<String, AgentConfigWrapper>()

    private ConfigWrapperContext() {
        agentConfigWrappers.with {
            it["Linux"] = new Linux()
        }
    }

    AgentConfigWrapper getWrapper(String platform) {
        def agentConfigWrapper = agentConfigWrappers[platform]
        if (!agentConfigWrapper) {
            throw new IllegalArgumentException("not found agent wrapper : " + platform)
        } 
        return agentConfigWrappers[platform] as AgentConfigWrapper
    }
}
