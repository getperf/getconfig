package com.getconfig.AgentWrapper

import com.getconfig.Utils.CommonUtil
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
    Map<String, DirectExecutorWrapper> directExecutorWrapper = new LinkedHashMap<>()

    private AgentWrapperManager() {
        agentWrappers.with {
            it["Linux"] = new Linux()
            it["Windows"] = new Windows()
            it["VMWare"] = new VMWare()
            it["VMHost"] = new VMHost()
            it["Solaris"] = new Solaris()
            it["CiscoUCS"] = new CiscoUCS()
            it["HPiLO"] = new HPiLO()
            it["Primergy"] = new Primergy()
            it["HitachiVSP"] = new HitachiVSP()
            it["NetApp"] = new NetApp()
            it["Zabbix"] = new Zabbix()
            it[AgentConstants.AGENT_LABEL_REMOTE] = new RemoteAgent()
            it[AgentConstants.AGENT_LABEL_LOCAL_FILE] = new LocalAgent()
        }
        directExecutorWrapper.with {
            it["Oracle"] = new Oracle()
            it["WindowsPy"] = new WindowsPy()
        }
    }

    String normalizePlatform(String platform, String domainExt) {
        if (domainExt != "") {
            platform = platform + CommonUtil.toCamelCase(domainExt, true)
        }
        return platform
    }

    AgentConfigWrapper getWrapper(String platform, String domainExt = "") {
        platform = normalizePlatform(platform, domainExt)
        def agentConfigWrapper = agentWrappers[platform]
        if (!agentConfigWrapper) {
            throw new IllegalArgumentException("not found agent wrapper : " + platform)
        } 
        return agentWrappers[platform] as AgentConfigWrapper
    }

    DirectExecutorWrapper getDirectExecutorWrapper(String platform, String domainExt = "") {
        platform = normalizePlatform(platform, domainExt)
        return directExecutorWrapper.get(platform) as DirectExecutorWrapper
    }
}
