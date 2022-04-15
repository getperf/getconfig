package com.getconfig.Document

import com.getconfig.Model.ReportSummary
import com.getconfig.Model.Result
import com.getconfig.Model.TestScenario
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class ReportMakerSummary {
    TestScenario testScenario
    ReportMaker reportMaker

    ReportMakerSummary(TestScenario testScenario, ReportMaker reportMaker) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
    }

    void make() {
        reportMaker.setTemplateSheet("Summary")
        reportMaker.copyTemplate("サマリレポート")
        SheetManager manager = reportMaker.createSheetManager()

        Map<String, Integer>headers = manager.parseHeaderComment()

        Map<String, String> domains = testScenario.getDomains()
        Map<String, ReportSummary.ReportColumn> summaryColumns =
                testScenario.reportSummary.getColumns()
        // ヘッダ編集
        def headerNames = headers.sort { a, b ->
            a.value <=> b.value }.keySet()
        manager.setPosition(0,0)
        headerNames.each {String headerName ->
            String headerLabel = summaryColumns.get(headerName)?.getName() ?: headerName
            manager.setCell(headerLabel, "HeaderServer")
        }
        int order = 1
        // ボディ編集
        manager.setPosition(1,0)

        testScenario.servers.each { String server ->
            List<String> platforms
            platforms = testScenario.serverPlatformKeys.get(server) as List<String>
            String resultSheetName = testScenario.getResultSheetName(server)
            headers.each { String columnId, int columnNo ->
                if (columnId == "no") {
                    manager.setCell(order, "Level")
                } else if (columnId == "hostName") {
                    manager.setCell(server)
                } else if (columnId == "domain") {
                    String domain = domains[server]
                    if (domain) {
                        reportMaker.setDetailLink(domain, manager.cell, "A1")
                        manager.setCell(domain, "Link")
                    }else {
                        manager.setCell('unknown')
                    }
                } else if (columnId == "ip") {
                    String ip = testScenario.portListKeys.get(server) ?: 'unknown'
                    manager.setCell(ip)
                } else {
                    ReportSummary.ReportColumn summaryColumn
                    summaryColumn = summaryColumns.get(columnId)
                    String metricId = summaryColumn.findMetricId(platforms as List)
                    if (metricId) {
                        Result result = testScenario.results.get(server, metricId)
                        if (result) {
                            // manager.setCell(result.value as String)
                            manager.setCell(result.value)
                        } else {
                            manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE)
                        }
                    } else {
                        manager.setCell(ExcelConstants.CELL_NOT_APPLICABLE_VALUE)
                    }
                }
            }
            manager.row.setHeightInPoints(ExcelConstants.REPORT_SUMMARY_ROW_HEIGHT)
            manager.nextRow()
            // manager.shiftRows()
            order++
        }
        // manager.removeHeaderComment(0)
        manager.setHeaderAutoFilter()
    }
}
