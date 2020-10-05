package com.getconfig.Model

import spock.lang.Specification

class TestScenarioTest extends Specification {
    void "初期化"() {
        when:
        def testScenario = new TestScenario()
        testScenario.platformServerKeys.put("Linux", "cent7")
        testScenario.platformServerKeys.put("VMWare", "cent7")
        testScenario.platformServerKeys.put("Linux", "cent8")
        testScenario.platformServerKeys.put("Windows", "w2016")
        testScenario.platformServerKeys.put("Windows", "w2016")
        testScenario.platformServerKeys.put("VMWare", "cent7")

        then:
        print testScenario.platformServerKeys
        1 == 1
    }
}
