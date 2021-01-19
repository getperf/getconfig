package com.getconfig.Document

import com.getconfig.Document.TestScenarioManager
import com.getconfig.Reporter
import com.getconfig.TestData
import spock.lang.Specification
import com.getconfig.Model.*

class ResultComparatorTest extends Specification {
    String metricLib = 'lib/dictionary'
    String reportPath = 'build/check_sheet.xlsx'
    Map<String, ResultGroup> testResultGroups
    TestScenario testScenario

    def setup() {
        testResultGroups = TestData.prepareResultGroupFromJson(
            'src/test/resources/node_compare_test')
        TestScenarioManager manager
        manager = new TestScenarioManager(this.metricLib, testResultGroups)
        manager.run()
        testScenario = manager.testScenario
    }

    def "比較結果レポート作成"() {
        when:
        Reporter reporter = new Reporter(this.testScenario, reportPath)
        println this.testScenario.servers
        new TagGenerator(testScenario).run()
        new ResultComparator(testScenario).run()
        reporter.initReport()
        reporter.makeResultReport()
        reporter.finishReport()

        then:
        1 == 1
    }

}
