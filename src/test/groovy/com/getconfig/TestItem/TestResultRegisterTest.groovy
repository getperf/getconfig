package com.getconfig.TestItem

import com.getconfig.Model.TestResult
import com.getconfig.TestItem
import spock.lang.Specification

class TestResultRegisterTest extends Specification {
    TestItem testItem = new TestItem("cent80", "Linux", "uname")

    def "値登録"() {
        when:
        testItem.results("4.18.0-147.el8.x86_64")

        then:
        TestResult testResult = testItem.testResultGroup.get("Linux", "uname")
        testResult.platform == "Linux"
        testResult.metricName == "uname"
        testResult.serverName == "cent80"
        testResult.value == "4.18.0-147.el8.x86_64"
    }

    def "複数の値登録"() {
        when:
        Map<String, Object> values = new LinkedHashMap<>()
        values.put("cpu", 4)
        values.put("memory", 8)
        values.put("arch", "x86_64")
        testItem.results(values)

        then:
        testItem.testResultGroup.get("Linux", "cpu").value == 4
        testItem.testResultGroup.get("Linux", "cpu").parentMetric == "uname"
        testItem.testResultGroup.get("Linux", "memory").value == 8
        testItem.testResultGroup.get("Linux", "arch").value == "x86_64"
    }

    def "エラー登録"() {
        when:
        testItem.error("not found uname")

        then:
        testItem.testResultGroup.get("Linux", "uname").error == "not found uname"
    }

    def "デバイス登録"() {
        when:
        List headers = ["A", "B", "C"]
        List csv = new ArrayList<>()
        csv << [1, 2, 3]
        csv << [4, 5, 6]

        testItem.devices(headers, csv)

        then:
        println testItem.testResultGroup.get("Linux", "uname").devices
        1 == 1
    }

    def "ポートリスト登録"() {
        when:
        testItem.portList("192.168.0.5", "ens192")

        then:
        println testItem.testResultGroup.portLists
        testItem.testResultGroup.portLists.size() > 0
    }
}
