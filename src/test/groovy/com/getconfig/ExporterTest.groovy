package com.getconfig

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class ExporterTest extends Specification {
    ConfigEnv env = ConfigEnv.instance

    void setup() {
        env.readConfig()
    }

    def "ドライラン"() {
        when:
        Exporter exporter = new Exporter()
        env.accept(exporter)
//        exporter.run()

        then:
        1 == 1
    }

}
