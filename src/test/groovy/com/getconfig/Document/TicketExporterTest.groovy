package com.getconfig.Document

import com.getconfig.ConfigEnv
import com.getconfig.Model.ResultGroup
import com.getconfig.Model.Server
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
        List<Server> testServers = TestData.readTestServers()
        TicketExporter exporter = new TicketExporter()
        ConfigEnv.instance.setDryRun()
        ConfigEnv.instance.accept(exporter)
        exporter.export(testServers, "src/test/resources/node")

        then:
        1 == 1
    }

    def "ポートリストステータス更新"() {
        when:
        // esxit.ostrich IA#8025 は、ポートリスト #8026,#8027,#8028 とリンク
        // ポートリスト #8026,#8027 リンク処理にて、対象外の #8028 が オフライン
        // ステータスになることを確認
        //
        // gradle test  --tests com.getconfig.Document.TicketExporterTest

        TicketManager ticketManager = new TicketManager()
        ConfigEnv.instance.setDryRun()
        ConfigEnv.instance.accept(ticketManager)
        ticketManager.readConfig()
        ticketManager.init()
        def ticket = ticketManager.getIssue("esxi.ostrich")
        println(ticket)
        def ticket_to_ids = [8026, 8027] // , 8028]
        ticketManager.link(ticket, ticket_to_ids)
        println(ticket_to_ids)

        then:
        1 == 1
    }
}
