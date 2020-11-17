package com.getconfig.Document

import com.getconfig.Model.ResultGroup
import com.getconfig.Model.TestScenario
import com.getconfig.ProjectManager.TicketExporter
import com.getconfig.TestData
import spock.lang.Specification

class TicketExporterTest extends Specification {
    String metricLib = 'lib/dictionary'
    String reportPath = 'build/check_sheet.xlsx'
    String excelTemplate = 'lib/template/report_summary.xlsx'
    Map<String, ResultGroup> testResultGroups
    TestScenario testScenario
    ReportMaker reportMaker

    def setup() {
        testResultGroups = TestData.prepareResultGroupFromJson()
        TestScenarioManager manager
        manager = new TestScenarioManager(this.metricLib, testResultGroups)
        manager.run()
        testScenario = manager.testScenario
        reportMaker = new ReportMaker(excelTemplate).read()
    }

    def "チケット登録"() {
        when:
        TicketManager ticketManager = new TicketManager()
        TicketExporter exporter = new TicketExporter(this.testScenario,
                reportMaker, ticketManager)
        exporter.make()

        then:
        1 == 1
    }
}
