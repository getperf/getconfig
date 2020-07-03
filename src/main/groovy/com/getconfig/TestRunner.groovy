package com.getconfig

import com.getconfig.AgentWrapper.AgentExecuter
import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import com.getconfig.Command.GetconfigCommand.RunCommand

// * コマンド引数の解析
// * Excel 仕様から検査対象サーバ読込み（委譲）
// * コレクターに引き渡して実行
// * パーサーに引き渡して実行
// * レポートに引き渡して実行

@Slf4j
@CompileStatic
class TestRunner {
    RunCommand runCommand

    TestRunner(RunCommand runCommand) {
        this.runCommand = runCommand
    }

    void setCommandArg(ConfigEnv env) {
        ConfigObject config = env.config
    }

    void run() {
        this.setCommandArg(ConfigEnv.instance)
        log.info("args: ${this.runCommand.level}")
        // def env = ConfigEnv.instance
        // env.readConfig(configFile)
        // def specReader = new SpecReader(inExcel : checkSheet)
        // specReader.parse()
        // specReader.mergeConfig()
        // def testServers = specReader.testServers()

        // Collector collector = new Collector(testServers)
        // collector.run()
    }
}
