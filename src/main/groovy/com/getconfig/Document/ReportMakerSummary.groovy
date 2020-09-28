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
        // この間でシートの編集操作を実行
        manager.setPosition(1,0)

        Map<String, String> domains = testScenario.getDomains()
        Map<String, ReportSummary.ReportColumn> summaryColumns =
                testScenario.reportSummary.getColumns()

        int order = 1
        testScenario.serverIndex.asMap().each {
            String server, Collection platforms ->
                headers.each { String columnId, int columnNo ->
                    if (columnId == "no") {
                        manager.setCellValue(order)
                    } else if (columnId == "hostName") {
                        manager.setCellValue(server)
                    } else if (columnId == "domain") {
                        manager.setCellValue(domains[server] ?: 'unknown')
                    } else if (columnId == "ip") {
                        String ip = testScenario.portListIndex.get(server) ?: 'unknown'
                        manager.setCellValue(ip)
                    } else {
                        ReportSummary.ReportColumn summaryColumn
                        summaryColumn = summaryColumns.get(columnId)
                        String metricId = summaryColumn.findMetricId(platforms as List)
                        if (metricId) {
                            Result result = testScenario.results.get(server, metricId)
                            if (result) {
                                manager.setCellValue(result.value as String)
                            } else {
                                manager.setCellValue(ExcelConstants.CELL_NOT_UNKOWN_VALUE)
                            }
                        } else {
                            manager.setCellValue(ExcelConstants.CELL_NOT_APPLICABLE_VALUE)
                        }
                    }
                }
                manager.nextRow()
                manager.shiftRows()
                order++
        }
    }
}
