package com.getconfig.Document

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.ResultGroup
import com.getconfig.Utils.DirUtils
import com.google.gson.Gson
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
class ResultGroupManager implements Controller {
    String nodeDir = "node"

    void setEnvironment(ConfigEnv env) {
        this.nodeDir = env.getProjectNodeDir()
    }

    void saveResultGroups(Map<String, ResultGroup> resultGroups) {
        resultGroups.each { String serverName, ResultGroup resultGroup ->
            saveResultGroup(serverName, resultGroup)
        }
    }

    void saveResultGroup(String serverName, ResultGroup resultGroup) {
        String jsonFile = Paths.get(this.nodeDir, "${serverName}.json")
        String jsonText = new Gson().toJson(resultGroup)
       new File(jsonFile).write(jsonText, "UTF-8")
    }

    Map<String, ResultGroup> readAllResultGroups() {
        List<String> serverNames = new ArrayList<>()
        File[] files = DirUtils.ls(this.nodeDir, "json")
        files.each {File jsonFile ->
            String fileName = jsonFile.getName()
            String serverName = fileName.substring(0, fileName.indexOf(".json"))
            serverNames << serverName
        }
        // println serverNames
        return readResultGroups(serverNames)
    }

    Map<String, ResultGroup> readResultGroups(List<String> serverNames) {
        Map<String, ResultGroup> resultGroups = new LinkedHashMap<>()
        serverNames.each { String serverName ->
            resultGroups.put(serverName, readResultGroup(serverName))
        }
        return resultGroups
    }

    ResultGroup readResultGroup(String serverName) {
        String jsonFile = Paths.get(this.nodeDir, "${serverName}.json")
        String json = new File(jsonFile).text
        ResultGroup resultGroup = new Gson().fromJson(json, ResultGroup.class)
        return resultGroup
    }

    int run() {
        return 0
    }
}
