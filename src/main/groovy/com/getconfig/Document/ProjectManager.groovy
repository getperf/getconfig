package com.getconfig.Document

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Document.ProjectManager.InventoryLoaderLocal
import com.getconfig.LogParser
import com.getconfig.Model.Server
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
class ProjectManager  implements Controller {
    String installDir
    String projectDir
    String projectLogDir
    String projectNodeDir
    String currentLogDir
    String currentNodeDir
    boolean dryRun
    List<GString> commands = new ArrayList<>()
    List<Server> testServers

    void setEnvironment(ConfigEnv env) {
        this.installDir = env.getGetconfigHome()
        this.projectDir = env.getProjectHome()
        this.projectLogDir = env.getProjectLogDir()
        this.projectNodeDir = env.getProjectNodeDir()
        this.currentLogDir = env.getCurrentLogDir()
        this.currentNodeDir = env.getCurrentNodeDir()
        this.dryRun = env.getDryRun()
    }

    void makeDirUnderProject(String base) throws IOException {
        String path = Paths.get(projectDir, base)
        if (!this.dryRun) {
            new File(path).mkdirs()
            String gitKeep = Paths.get(path, '.gitkeep')
            new File(gitKeep).createNewFile()
        }
        this.commands << "create '${path}'"
    }

    void copyAbsoluteFile(String sourcePath, String targetPath)
            throws IOException {
        if (!this.dryRun) {
            FileUtils.copyFile(new File(sourcePath), new File(targetPath))
        }
        this.commands << "copy '${sourcePath}' '${targetPath}'"
    }

    void copyFile(String baseDir, String source, String target = null)
            throws IOException {
        String sourcePath = Paths.get(installDir, baseDir, source)
        String targetPath = Paths.get(projectDir, baseDir, target ?: source)
        copyAbsoluteFile(sourcePath, targetPath)
    }

    void copyAbsoluteDir(String sourcePath, String targetPath = null) 
        throws IOException {
        if (!this.dryRun) {
            FileUtils.copyDirectory(new File(sourcePath),new File(targetPath))
        }
        this.commands << "copy dir '${sourcePath}' '${targetPath}'"
    }

    void copyDir(String source, String target = null) {
        String sourcePath = Paths.get(installDir, source)
        String targetPath = Paths.get(projectDir, target ?: source)
        copyAbsoluteDir(sourcePath, targetPath)
    }

    void copyNodeToProject() {
        copyAbsoluteDir(this.currentNodeDir, this.projectNodeDir)
    }

    void copyInventoryLogToProject() {
        copyAbsoluteDir(this.currentLogDir, this.projectLogDir)
//         LogParser logParser = new LogParser(testServers)
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

    void initProject(String projectDir, String mode = 'normal') throws IOException {
        this.projectDir = projectDir
        String targetDir = new File(projectDir).getAbsolutePath()
        if (new File(targetDir).exists()) {
            throw new IllegalArgumentException("already exists : ${targetDir}")
        }
        // create an empty directory
        List<String> createDirs = ['config', 'build', 'node']
        if (mode == 'detail') {
            createDirs << 'src/test/resources/inventory'
        }
        createDirs.each {String createDir ->
            this.makeDirUnderProject(createDir)
        }
        // copy config file under home
        ['config.groovy'].each {base ->
            this.copyFile('config', 'config.groovy')
        }
        // copy all files under the directory
        List<String> copyDirs = ['lib']
        if (mode == 'detail') {
            copyDirs << 'src/test/resources/inventory'
        }
        copyDirs.each { base ->
            this.copyDir(base)
        }
        // Copy Excel file under home
        if (mode == 'detail') {
            new File(installDir).eachFileMatch(FileType.FILES, ~/.+.xlsx/) {
                if (it.name=~/^blank_/) {
                    return
                }
                this.copyFile('', it.name)
            }
        } else {
            // Search "blank_*.xlsx" and copy it with the file name excluding "blank_".
            new File(installDir).eachFileMatch(FileType.FILES, ~/blank_.+.xlsx/) {
                String source = it.name
                def target = source.replaceFirst(/blank_/, "")
                this.copyFile('', source, target)
            }
        }
        ['.gitignore', 'Changes.txt', 'Readme.md', 'LICENSE.txt',].each { base ->
            this.copyFile('', base)
        }
        if (this.dryRun) {
            this.printCommands()
        } else {
            log.info("created '${projectDir}'")
        }
    }

    void update(List<Server> testServers, String mode = 'local') throws IOException {
        this.testServers = testServers
        switch (mode) {
            case 'local' :
//                InventoryLoaderLocal loader = new InventoryLoaderLocal()
                this.copyInventoryLogToProject()
                this.copyNodeToProject()
                break

            case 'db' :
                println 'this.export_cmdb()'
                break

            default :
                throw new IllegalArgumentException("unkown mode : ${mode}")
        }
//        if (this.dryRun) {
//            this.printCommands()
//        } else {
//            log.info("updated '${projectDir}'")
//        }
    }

    void printCommands() {
        this.commands.each { String command ->
            println command
        }
    }

    int run() {
    }
}
