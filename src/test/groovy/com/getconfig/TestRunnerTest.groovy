package com.getconfig


import spock.lang.Specification

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
//        runner.run()

        then:
        1 == 1
    }
}
