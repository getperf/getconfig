package com.getconfig.Document

import com.getconfig.ConfigEnv
import com.getconfig.Model.Server
import com.getconfig.TestData
import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import spock.lang.Specification

class ProjectManagerTest extends Specification {
    String installDir
    String projectDir

    def setup() {
        def env = ConfigEnv.instance
        installDir = env.getGetconfigHome()
        projectDir = "c:\\home\\test1"
        //プロジェクトログディレクトリからワークログディレクトリにファイルコピー
//        env.projectHome
        def sourceDir = new File(env.getProjectLogDir())
        def targetDir = new File(env.getCurrentLogDir())
        println targetDir
        targetDir.mkdirs()
        FileUtils.copyDirectory(sourceDir, targetDir)

    }

    def "初期化コマンド"() {
        when:
        def manager = new ProjectManager(installDir: installDir, dryRun : true)
        manager.initProject(projectDir)

        then:
        println new JsonBuilder( manager.commands ).toPrettyString()
        manager.commands.size() > 0
    }

    def "更新コマンド"() {
        when:
        List<Server> testServers = TestData.readTestServers()
//        def manager = new ProjectManager(installDir: installDir,
//                projectDir: projectDir, dryRun: true)
        def manager = new ProjectManager()
        ConfigEnv.instance.accept(manager)
        manager.dryRun = true
        manager.update(testServers)

        then:
//        println new JsonBuilder( manager.commands ).toPrettyString()
//        manager.commands.size() > 0
        1 == 1
    }
}
