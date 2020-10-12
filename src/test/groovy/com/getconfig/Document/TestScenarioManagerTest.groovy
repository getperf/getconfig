package com.getconfig.Document


import com.getconfig.TestData
import com.getconfig.Model.*
import spock.lang.Specification

class TestScenarioManagerTest extends Specification {
    String metricLib = 'lib/dictionary'
    Map<String, ResultGroup> testResultGroups

    def setup() {
        testResultGroups = TestData.prepareResultGroupFromJson()
    }

    def "シナリオ変換"() {
        when:
        TestScenarioManager manager = new TestScenarioManager(this.metricLib)
        TestScenario testScenario = manager.setResultGroups(testResultGroups)
//        TestScenario testScenario = manager.readMetrics()

        then:
        testScenario.with {
            portLists.get("centos80.192.168.0.5").ip == "192.168.0.5"
            portListKeys.get("centos80").getAt(0) == "192.168.0.5"
            serverPlatformKeys.get("centos80") as Set == ["VMWare", "Linux" ]
            platformServerKeys.get("VMWare") as Set == ["centos80", "w2016"]
            results.rowKeySet() as Set == ["centos80", "esxi.ostrich",
                                           "server01","server02",
                                           "w2016"]
            results.column("Linux.uname")*.getKey() as Set
                    == ["centos80", "server01"]
            results.get("centos80", "Linux.uname").value
                    == "Linux centos80.getperf 4.18.0-147.el8.x86_64"
            results.get("centos80","Linux.kernel").value
                    == "Linux centos80.getperf 4.18.0-147.el8"
        }
    }

    def "レポート定義読込み"() {
        when:
        Map<String, ResultGroup> testResultGroups = TestData.prepareResultGroupFromJson()
        TestScenarioManager manager = new TestScenarioManager(this.metricLib)
        manager.readReportConfig()
        TestScenario testScenario = manager.readMetrics()

        then:
        testScenario.reportResult.findSheet(["Linux"]).name == "Linux(オンプレ)"
        println testScenario.reportSummary.columns.get("hostName").name == "ホスト名"
    }

    def "メトリック登録"() {
        when:
        TestScenarioManager manager = new TestScenarioManager(this.metricLib)
        manager.setResultGroups(testResultGroups)
        TestScenario testScenario = manager.readMetrics()
        manager.setAddedMetrics(testResultGroups)

        then:
        testScenario.with {
            metrics.get("Linux.000000001.000000000").id == "hostname"
//            metrics.get("Linux.000000055.000000002").id == "user.root.group"
            metricIndex.get("Linux.uname") == "Linux.000000003.000000000"
            metricIndex.get("Linux.user.root.home") == "Linux.000000055.000000001"
        }
    }

    def "対象サーバ登録"() {
        when:
        TestScenarioManager manager = new TestScenarioManager(this.metricLib)
        manager.setResultGroups(testResultGroups)
        manager.readReportConfig()
        TestScenario testScenario = manager.setServerToReport()

        then:
        testScenario.with {
            resultSheetServerKeys.get("Linux(VM)") == ["centos80"]
            resultSheets.get("Linux(VM)") != null
        }
    }

    def "シナリオ作成実行"() {
        when:
        Map<String, ResultGroup> testResultGroups
        testResultGroups = TestData.prepareResultGroupFromJson()
        TestScenarioManager manager = new TestScenarioManager(this.metricLib,
                                            testResultGroups)
        int result = manager.run()

        then:
        manager.testScenario.servers[0] == "centos80"
        manager.testScenario.resultSheetServerKeys.get("Linux(VM)") != null
        result == 0
    }
}
