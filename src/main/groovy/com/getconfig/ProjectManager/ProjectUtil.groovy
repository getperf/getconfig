package com.getconfig.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import org.apache.commons.io.FileUtils

import java.nio.file.Paths

class ProjectUtil implements Controller {
    String installDir
    String projectDir
    String projectLogDir
    String projectNodeDir
    String currentLogDir
    String currentNodeDir
    boolean dryRun
    List<GString> commands = new ArrayList<>()

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

    void printCommands() {
        this.commands.each { String command ->
            println command
        }
    }

    int run() {
    }
}
