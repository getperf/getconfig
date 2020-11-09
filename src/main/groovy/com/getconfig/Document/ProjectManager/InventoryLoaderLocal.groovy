package com.getconfig.Document.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.Server
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class InventoryLoaderLocal implements Controller {
    String projectHome
    String projectLogDir
    String projectNodeDir
    String currentLogDir
    String currentNodeDir
    boolean dryRun
    List<Server> testServers
    ProjectUtil projectUtil = new ProjectUtil()

    void setEnvironment(ConfigEnv env) {
        this.projectHome = env.getProjectHome()
        this.projectLogDir = env.getProjectLogDir()
        this.projectNodeDir = env.getProjectNodeDir()
        this.currentLogDir = env.getCurrentLogDir()
        this.currentNodeDir = env.getCurrentNodeDir()
        this.dryRun = env.getDryRun()
        env.accept(projectUtil)
    }

    void copyNodeToProject() {
        projectUtil.copyAbsoluteDir(this.currentNodeDir, this.projectNodeDir)
    }

    void copyInventoryLogToProject() {
        projectUtil.copyAbsoluteDir(this.currentLogDir, this.projectLogDir)
//        LogParser logParser = new LogParser(testServers)
//        logParser.agentLogPath = this.currentLogDir
//        try {
//            logParser.makeAgentLogLists()
//        } catch(Exception e) {
//            println e.stackTrace
//        }
//         logParser.agentLogs.each() { AgentLog agentLog ->
//             String source = agentLog.getLogPath()
//             String destRelPath = agentLog.getProjectLogPath()
//             if (destRelPath) {
//                 String dest = Paths.get(this.projectNodeDir, destRelPath)
//                 this.copyAbsoluteDir(source, dest)
//             }
//         }
    }

    int run() {
        this.copyInventoryLogToProject()
        this.copyNodeToProject()
        if (this.dryRun) {
            projectUtil.printCommands()
        } else {
            log.info("updated")
        }
        return 0
    }
}
