package com.getconfig.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.zeroturnaround.zip.NameMapper
import org.zeroturnaround.zip.ZipUtil

import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
class BackupManager implements Controller {
    String installDir
    ProjectUtil projectUtil = new ProjectUtil()

    void setEnvironment(ConfigEnv env) {
        this.installDir = env.getGetconfigHome()
        env.accept(projectUtil)
    }

    File makeProjectDir(String projectDirName) {
        if (projectDirName == ".") {
            projectDirName = Paths.get("").toAbsolutePath()
        }
        return new File(projectDirName)
    }

    File makeZipPath(File zipDir) {
        String zip = Paths.get(zipDir.getParent(), zipDir.getName() + ".zip")
        return new File(zip)
    }

    boolean checkProjectDirectory(File projectDir) {
        File checkSheet = new File(projectDir, "getconfig.xlsx")
        if (checkSheet.exists()) {
            return true
        } else {
            log.error("not found check sheet : '${checkSheet}'")
            return false
        }
    }

    void backupProject(File zipPath, String projectDirName, Boolean force = false) {
        File projectDir = makeProjectDir(projectDirName)
        if (!zipPath) {
            zipPath = makeZipPath(projectDir)
        }
        if (!force && !checkProjectDirectory(projectDir)) {
            throw new IllegalArgumentException(
                    "Not a project directory. If you want to force it, add the --force option.")
        }
        log.info "backup '${projectDir}'. save '${zipPath}'."
        ZipUtil.pack(projectDir, zipPath, new NameMapper() {
            @Override
            String map(String name) {
                return projectDir.getName() + "/" + name
            }
        })
    }

    int run() {
    }

}
