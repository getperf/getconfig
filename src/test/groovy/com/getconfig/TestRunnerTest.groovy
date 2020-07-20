package com.getconfig

import com.getconfig.Model.TestServer
import spock.lang.Specification
import com.getconfig.Command.GetconfigCommand.RunCommand

class TestRunnerTest extends Specification {

    void setup() {
        ConfigEnv.instance.readConfig()
    }

    def "Run"() {
        when:
        def runner = new TestRunner(
                dryRun: true,
                checkSheetPath: "サーバチェックシート.xlsx",
        )
        runner.run()

        then:
        1 == 1
    }
}
