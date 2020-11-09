package com.getconfig.Document.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class ProjectInitializer implements Controller {
    String installDir
    // String projectDir
    ProjectUtil projectUtil = new ProjectUtil()

    void setEnvironment(ConfigEnv env) {
        this.installDir = env.getGetconfigHome()
        // this.projectDir = env.getProjectHome()
        env.accept(projectUtil)
    }

    void initProject(String projectDir, String mode = 'normal') throws IOException {
        projectUtil.projectDir = projectDir
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
            projectUtil.makeDirUnderProject(createDir)
        }
        // copy config file under home
        ['config.groovy'].each {base ->
            projectUtil.copyFile('config', 'config.groovy')
        }
        // copy all files under the directory
        List<String> copyDirs = ['lib']
        if (mode == 'detail') {
            copyDirs << 'src/test/resources/inventory'
        }
        copyDirs.each { base ->
            projectUtil.copyDir(base)
        }
        // Copy Excel file under home
        if (mode == 'detail') {
            new File(installDir).eachFileMatch(FileType.FILES, ~/.+.xlsx/) {
                if (it.name=~/^blank_/) {
                    return
                }
                projectUtil.copyFile('', it.name)
            }
        } else {
            // Search "blank_*.xlsx" and copy it with the file name excluding "blank_".
            new File(installDir).eachFileMatch(FileType.FILES, ~/blank_.+.xlsx/) {
                String source = it.name
                def target = source.replaceFirst(/blank_/, "")
                projectUtil.copyFile('', source, target)
            }
        }
        ['.gitignore', 'Changes.txt', 'Readme.md', 'LICENSE.txt',].each { base ->
            projectUtil.copyFile('', base)
        }
        if (projectUtil.dryRun) {
            projectUtil.printCommands()
        } else {
            log.info("created '${projectDir}'")
        }
    }

    int run() {
    }
}
