package com.getconfig

import spock.lang.Specification
import com.getconfig.Command.GetconfigCommand.RunCommand

class TestRunnerTest extends Specification {
    RunCommand runCommand
    void setup() {
        runCommand = new RunCommand()
        runCommand.level = 2
    }

    def "Run"() {
        when:
        def runner = new TestRunner(runCommand)
        runner.run()

        then:
        println runner.runCommand.level
//        println runner
        1 == 1
    }
}
