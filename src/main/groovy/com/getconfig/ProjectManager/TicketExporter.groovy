package com.getconfig.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Document.ReportMaker
import com.getconfig.Document.ResultGroupManager
import com.getconfig.Document.SheetManager
import com.getconfig.Document.TestScenarioManager
import com.getconfig.Document.TicketManager
import com.getconfig.Model.PortList
import com.getconfig.Model.ReportSummary
import com.getconfig.Model.Result
import com.getconfig.Model.ResultGroup
import com.getconfig.Model.Server
import com.getconfig.Model.TestScenario
import com.getconfig.Model.Ticket
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class TicketExporter implements Controller {
    String redmineConfigPath = 'lib/dictionary/redmine.toml'
    String excelTemplatePath = 'lib/template/report_summary.xlsx'
    String metricLib = 'lib/dictionary'
    String nodeDir = 'src/test/resources/node'
    TicketManager ticketManager = new TicketManager()

    void setEnvironment(ConfigEnv env) {
        env.accept(ticketManager)
        this.redmineConfigPath = env.getRedmineConfigPath()
        this.excelTemplatePath = env.getExcelTemplatePath()
        this.metricLib = env.getMetricLib()
        this.nodeDir = env.getProjectNodeDir()
    }

    Map<String, ResultGroup> readResultGroup(List<String> servers, String nodeDir) {
        ResultGroupManager manager
        manager = new ResultGroupManager(nodeDir: nodeDir)
        return manager.readResultGroups(servers)
    }

    TestScenario getTestScenario(List<Server> servers, String nodeDir) {
        this.nodeDir = nodeDir
        Map<String, ResultGroup> testResultGroups
        List<String> serverNames = servers*.serverName?.unique()
        testResultGroups = readResultGroup(serverNames, nodeDir)
        TestScenarioManager manager = new TestScenarioManager(
                this.metricLib, testResultGroups)
        manager.run()
        return manager.testScenario
    }

    List<String> getHeaders(String excelTemplate) {
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.setTemplateSheet("Summary")
        reportMaker.copyTemplate("サマリレポート")
        SheetManager manager = reportMaker.createSheetManager()
        Map<String, Integer> headers = manager.parseHeaderComment()
        return headers?.keySet() as List<String>
    }

    String customFieldValue(Object value) {
        // 値が浮動小数点の場合は丸め値に変換する
        if (value in Double) {
            double roundValue = Math.round(value as double)
            return roundValue as String
        // Redmine カスタムフィールドの真偽値(bool_cf)は文字列の"0"か"1"を返す必要がある
        } else if (value in Boolean) {
            return (value) ? "1" : "0"
        } else {
            return value as String
        }
    }

    Ticket makeCustomFields(Ticket ticket, TestScenario testScenario,
                            List<String> headers, String server) {
        Map<String, ReportSummary.ReportColumn> summaryColumns =
            testScenario.reportSummary.getColumns()
        List<String> platforms =
            testScenario.serverPlatformKeys.get(server) as List<String>
        headers.each { String columnId ->
            ReportSummary.ReportColumn summaryColumn
            summaryColumn = summaryColumns.get(columnId)
            if (summaryColumn) {
                String redmineField = summaryColumn.redmineField
                String metricId = summaryColumn.findMetricId(platforms as List)
                if (redmineField && metricId) {
                    Result result = testScenario.results.get(server, metricId)
                    if (result) {
                        String value = customFieldValue(result.value)
                        ticket.custom_fields.put(redmineField, value)
                    }
                }
            }
        }
        return ticket
    }

    void export(List<Server> testServers, String nodeDir) {
        TestScenario testScenario = getTestScenario(testServers, nodeDir)
        List<String> headers = getHeaders(this.excelTemplatePath)
        Map<String, String> domains = testScenario.getDomains()
        Map<String, ReportSummary.ReportColumn> summaryColumns =
                testScenario.reportSummary.getColumns()

        ticketManager.readConfig()
        ticketManager.init()
        testScenario.servers.each { String server ->
            String tracker = testScenario.getTracker(server)
            if (!tracker) {
                return
            }
            String domain = domains?.get(server)
            Ticket ticket = new Ticket(server, tracker, domain)
            ticket = makeCustomFields(ticket, testScenario, headers, server)
            testScenario.portListKeys.get(server)?.each { String ip ->
                PortList portList = testScenario.getPortList(server, ip)
                ticket.addPortList(portList)
            }
            ticketManager.resister(ticket)
        }
    }

    void showConfig() {
        this.ticketManager.showConfig()
    }

    int run() {
        return 0
    }

}
