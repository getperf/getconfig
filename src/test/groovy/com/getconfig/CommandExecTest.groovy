package com.getconfig

import spock.lang.Specification
import java.nio.file.Paths

class CommandExecTest extends Specification {


    def "Run with timeout"() {
        when:
        def exec = new CommandExec(3000)
        String[] args
        String osName = System.properties['os.name']
        if (osName.toLowerCase().contains("windows")) {
            args = ["/n", "5", "/w", "1000", "127.0.0.1"]
        }else {
            args = ["-c", "5", "-i", "1", "127.0.0.1"]
        }
        def rc = exec.run("ping", args)

        then:
        rc == 1
    }
}
