package com.getconfig.AgentWrapper

import com.getconfig.Utils.DirUtils
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
    private GroovyClassLoader gcl = new GroovyClassLoader();

    private Collection<Class> loadClassFromFile(String path) throws IOException {
        gcl.clearCache();
        File[] files = DirUtils.ls(path, "groovy");

        List<Class> classes = new ArrayList<>();
        for (File file : files) {
            println "load file = ${file.getAbsolutePath()}"
            Class cls = gcl.parseClass(file);
            classes.add(cls);
        }
        return classes;
    }

    String getDomainName(String className) {
        if (className == "RemoteAgent") {
            return "{Agent}"
        } else if (className == "LocalAgent") {
            return "{LocalFile}"
        } else {
            return className
        }
    }

    private void load(Collection<Class> classes){
        for (Class cls : classes) {
            try {
                Object o = cls.newInstance();
                String domainName = this.getDomainName(cls.getSimpleName())
                agentWrappers.put(domainName, o as AgentConfigWrapper)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void init(String agentWrapperLibPath = null) {
        Collection<Class> fileClasses = this.loadClassFromFile(agentWrapperLibPath ?: "lib/agentconf")
        this.load(fileClasses)
    }

    AgentConfigWrapper getWrapper(String platform) {
        def agentConfigWrapper = agentWrappers[platform]
        if (!agentConfigWrapper) {
            throw new IllegalArgumentException("not found agent wrapper : " + platform)
        } 
        return agentWrappers[platform] as AgentConfigWrapper
    }
}
