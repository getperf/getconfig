package com.getconfig.Document

import com.getconfig.Model.Metric
import com.getconfig.Model.MetricId
import com.getconfig.Model.ReportSummary
import com.getconfig.Model.Result
import com.getconfig.Model.ResultSheet
import com.getconfig.Model.TestScenario
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class ReportMakerResult {

    TestScenario testScenario
    ReportMaker reportMaker

    ReportMakerResult(TestScenario testScenario, ReportMaker reportMaker) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
    }

    void make() {
        reportMaker.parseCellStyles("CellStyle")

        testScenario.resultSheetServers.asMap().each {
            String sheetName, Collection<String> servers ->
            reportMaker.setTemplateSheet("TestResult")
            reportMaker.copyTemplate(sheetName)
            SheetManager manager = reportMaker.createSheetManager()
            manager.setPosition(0, 7)
            servers.each { String server ->
                manager.setCell(server, "HeaderServer")
            }
            manager.setCell("TagResult", "HeaderTag")
            manager.setPosition(1, 0)
            ResultSheet resultSheet = testScenario.getResultSheet(sheetName)
            ["HW", "OS", "MONITOR"].each { String platformCategory ->
                List<String> platforms = resultSheet.platforms.get(platformCategory)
                if (!platforms) {
                    return
                }
                platforms.each { String platform ->
                    testScenario.metricKeyIndex.get(platform).sort().each {
                        String metricKey ->
                            Metric metric = testScenario.metrics.get(metricKey)
                            manager.setCellValue(metric.level)
                            manager.setCellValue(metric.category)
                            manager.setCellValue(metric.name)
                            manager.setCellValue(metric.id)
                            manager.setCellValue(metric.deviceFlag?.toString())
                            manager.setCellValue(metric.comment)
                            manager.setCellValue(platform)
                            servers.each { String server ->
                                String metricId = MetricId.make(platform, metric.id)
                                Result result = testScenario.results.get(server, metricId)
                                if (result) {
                                    manager.setCell(result.value as String, "Normal")
                                } else {
                                    manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
                                }
                            }
                            manager.setCell("100%", "Normal")
                            manager.nextRow()
                            manager.shiftRows()
                    }
                }
            }
        }
    }
}