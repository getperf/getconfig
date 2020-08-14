package com.getconfig.Testing

import com.getconfig.Model.TestResult
import spock.lang.Specification

class TestResultRegisterTest extends Specification {
    TestUtil t = new TestUtil("cent80", "Linux", "uname")

    def "値登録"() {
        when:
        t.results("4.18.0-147.el8.x86_64")

        then:
        TestResult testResult = t.testResultGroup.get("Linux", "uname")
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
        t.results(values)

        then:
        t.testResultGroup.get("Linux", "cpu").value == 4
        t.testResultGroup.get("Linux", "cpu").parentMetric == "uname"
        t.testResultGroup.get("Linux", "memory").value == 8
        t.testResultGroup.get("Linux", "arch").value == "x86_64"
    }

    def "エラー登録"() {
        when:
        t.error("not found uname")

        then:
        t.testResultGroup.get("Linux", "uname").error == "not found uname"
    }

    def "デバイス登録"() {
        when:
        List headers = ["A", "B", "C"]
        List csv = new ArrayList<>()
        csv << [1, 2, 3]
        csv << [4, 5, 6]

        t.devices(headers, csv)

        then:
        println t.testResultGroup.get("Linux", "uname").devices
        1 == 1
    }

    def "ポートリスト登録"() {
        when:
        t.portList("192.168.0.5", "ens192")

        then:
        println t.portListGroup.portLists
        t.portListGroup.portLists.size() > 0
    }

    def "メトリック追加"() {
        when:
        t.newMetric("uname.hoge", "uname hoge", "abc")

        then:
        t.get("Linux", "uname.hoge").value == "abc"
        t.addedTestMetrics.get("uname.hoge").with {
            parentMetric == "uname"
            comment == "uname hoge"
        }
    }

}
