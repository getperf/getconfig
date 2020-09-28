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
        reporter.initReport()
        reporter.makeResultReport()
        reporter.finishReport()

        then:
        1 == 1
    }
}
