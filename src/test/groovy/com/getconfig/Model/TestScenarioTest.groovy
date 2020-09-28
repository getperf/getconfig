package com.getconfig.Model

import spock.lang.Specification

class TestScenarioTest extends Specification {
    void "初期化"() {
        when:
        def testScenario = new TestScenario()
        testScenario.platformIndex.put("Linux", "cent7")
        testScenario.platformIndex.put("VMWare", "cent7")
        testScenario.platformIndex.put("Linux", "cent8")
        testScenario.platformIndex.put("Windows", "w2016")
        testScenario.platformIndex.put("Windows", "w2016")
        testScenario.platformIndex.put("VMWare", "cent7")

        then:
        print testScenario.platformIndex
        1 == 1
    }
}
