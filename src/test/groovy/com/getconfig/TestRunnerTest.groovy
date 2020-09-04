package com.getconfig

import com.getconfig.Model.TestServer
import spock.lang.Specification
import com.getconfig.Command.GetconfigCommand.RunCommand

class TestRunnerTest extends Specification {

    void setup() {
        ConfigEnv.instance.readConfig()
    }

    def "ドライラン"() {
        when:
        def runner = new TestRunner(
                checkSheetPath: "サーバチェックシート.xlsx",
        )
        ConfigEnv.instance.setDryRun()
        runner.run()

        then:
        1 == 1
    }
}
