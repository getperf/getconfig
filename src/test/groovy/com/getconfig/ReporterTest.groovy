package com.getconfig

import com.getconfig.Document.TestScenarioManager
import spock.lang.Specification
import com.getconfig.Model.*

class ReporterTest extends Specification {
    String metricLib = 'lib/dictionary'
    String reportPath = 'build/check_sheet.xlsx'
    Map<String, ResultGroup> testResultGroups
    TestScenario testScenario

    def setup() {
        testResultGroups = TestData.prepareResultGroupFromJson()
        TestScenarioManager manager
        manager = new TestScenarioManager(this.metricLib, testResultGroups)
        manager.run()
        testScenario = manager.testScenario
    }

    def setup_compare_test() {
        testResultGroups = TestData.prepareResultGroupFromJson(
            'src/test/resources/node_compare_test')
        TestScenarioManager manager
        manager = new TestScenarioManager(this.metricLib, testResultGroups)
        manager.run()
        testScenario = manager.testScenario
    }

    def "サマリレポート作成"() {
        when:
        Reporter reporter = new Reporter(this.testScenario, reportPath)
        reporter.initReport()
        reporter.makeSummaryReport()
        reporter.finishReport()

        then:
        1 == 1
    }

    def "結果レポート作成"() {
        when:
        Reporter reporter = new Reporter(this.testScenario, reportPath)
        println this.testScenario.servers
        reporter.initReport()
        reporter.makeResultReport()
        reporter.finishReport()

        then:
        1 == 1
    }

//    def "比較結果レポート作成"() {
//        when:
//        // setup_compare_test()
//        testResultGroups = TestData.prepareResultGroupFromJson(
//            'src/test/resources/node_compare_test')
//        println testResultGroups.keySet()
//        TestScenarioManager manager
//        manager = new TestScenarioManager(this.metricLib, testResultGroups)
//        manager.run()
//        testScenario = manager.testScenario
//        Reporter reporter = new Reporter(this.testScenario, reportPath)
//        reporter.initReport()
//        reporter.makeResultReport()
//        reporter.finishReport()
//
//        then:
//        1 == 1
//    }

    def "デバイスレポート作成"() {
        when:
        Reporter reporter = new Reporter(this.testScenario, reportPath)
        reporter.initReport()
        reporter.makeDeviceReport()
        reporter.finishReport()

        then:
        1 == 1
    }


}
