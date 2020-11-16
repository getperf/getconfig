package com.getconfig.Document

import com.getconfig.Model.PortList
import com.getconfig.Model.ReportSummary
import com.getconfig.Model.Result
import com.getconfig.Model.ResultSheet
import com.getconfig.Model.TestScenario
import com.getconfig.Model.Ticket
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class TicketExporter {
    TestScenario testScenario
    ReportMaker reportMaker
    TicketManager ticketManager

    TicketExporter(TestScenario testScenario, ReportMaker reportMaker,
                   TicketManager ticketManager) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
        this.ticketManager = ticketManager
    }

    List<String> getHeaders() {
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

    void make() {
        List<String> headers = getHeaders()
        Map<String, String> domains = testScenario.getDomains()
        Map<String, ReportSummary.ReportColumn> summaryColumns =
                testScenario.reportSummary.getColumns()

        testScenario.servers.each { String server ->
            List<String> platforms
            platforms = testScenario.serverPlatformKeys.get(server) as List<String>
            String tracker = testScenario.getTracker(server)
            if (!tracker) {
                return
            }
            String domain = domains?.get(server)
            Ticket ticket = new Ticket(server, tracker, domain)
            Map<String,String> fields = new LinkedHashMap<>()

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
            println "TICKET:${ticket}"
            testScenario.portListKeys.get(server)?.each { String ip ->
                PortList portList = testScenario.getPortList(server, ip)
                println "PORTLIST:${portList}"
            }
        }
    }
}
