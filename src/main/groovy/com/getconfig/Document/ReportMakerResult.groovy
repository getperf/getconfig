package com.getconfig.Document

import com.getconfig.Model.Metric
import com.getconfig.Model.MetricId
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

        testScenario.resultSheetServerKeys.asMap().each {
            String sheetName, Collection<String> servers ->
            reportMaker.setTemplateSheet("TestResult")
            reportMaker.copyTemplate(sheetName)
            SheetManager manager = reportMaker.createSheetManager()
            manager.setPosition(0, 7)
            servers.each { String server ->
                manager.setCell(server, "HeaderServer")
            }
            // manager.setCell("TagResult", "HeaderTag")
            manager.setPosition(1, 0)
            ResultSheet resultSheet = testScenario.getResultSheet(sheetName)
            int row = 1
            ExcelConstants.RESULT_SHEET_PLATFORM_CATEGORY_ORDER.each {
                String platformCategory ->
                List<String> platforms = resultSheet.platforms.get(platformCategory)
                if (!platforms) {
                    return
                }
                platforms.each { String platform ->
                    testScenario.platformMetricKeys.get(platform).sort().each {
                        String metricKey ->
                            manager.setPosition(row,0)
                            Metric metric = testScenario.metrics.get(metricKey)
                            int level = (metric.level < 0) ? 0 : metric.level
                            manager.setCell(level, "Level")
                            manager.setCell(metric.category)
                            manager.setCell(metric.name)
                            manager.setCell(metric.id)
                            manager.setCell(metric.deviceFlag?.toString())
                            manager.setCell(metric.comment)
                            manager.setCell(platform)
                            servers.each { String server ->
                                String metricId = MetricId.make(platform, metric.id)
                                Result result = testScenario.results.get(server, metricId)
                                if (result) {
                                    manager.setCell(result.value as String, "Normal")
                                } else {
                                    manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
                                }
                            }
                            // manager.setCell("100%", "Normal")
                            row ++
                            // manager.nextRow()
                            // manager.shiftRows()
                    }
                }
            }
        }
    }
}