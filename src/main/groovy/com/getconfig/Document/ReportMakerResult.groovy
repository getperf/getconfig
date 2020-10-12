package com.getconfig.Document

import com.getconfig.Model.Metric
import com.getconfig.Model.MetricId
import com.getconfig.Model.Result
import com.getconfig.Model.ResultSheet
import com.getconfig.Model.TestScenario
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Hyperlink
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.common.usermodel.Hyperlink

@Slf4j
@TypeChecked
@CompileStatic
class ReportMakerResult {

    TestScenario testScenario
    ReportMaker reportMaker
    SheetManager manager
    List<Integer> groupRows

    ReportMakerResult(TestScenario testScenario, ReportMaker reportMaker) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
    }

    void makeHeader(List<String> servers) {
        int columnPosition = 7
        manager.sheet.setColumnHidden(5, true)
        manager.setPosition(0, columnPosition)
        servers.each { String server ->
            manager.setCell(server, "HeaderServer")
            manager.sheet.setColumnWidth(columnPosition, 48*256)
            columnPosition ++
        }
    }

    void makeRow(int row, Metric metric, List<String> servers) {
        manager.setPosition(row,0)
        String platform = metric.platform
        String metricId = MetricId.make(platform, metric.id)

        int level = (metric.level < 0) ? 0 : metric.level
        manager.setCell(level, "Level")
        manager.setCell(metric.category)
        manager.setCell(metric.name)
        manager.setCell(metric.id)
        def device = testScenario.devices.column(metricId)
        if (device){
            reportMaker.setDetailLink(metricId, manager.cell, "A1")
            manager.setCell("詳細", "Link")
        } else {
            manager.setCell("")
        }
        manager.setCell(metric.comment)
        manager.setCell(platform)

        servers.each { String server ->
            Result result = testScenario.results.get(server, metricId)
            if (result) {
                manager.setCell(result.value)
            } else {
                manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
            }
        }
    }

    void addCategoryRow(int row, Metric metric, List<String> servers) {
        manager.setPosition(row,0)
        String platform = metric.platform
        String metricId = MetricId.make(platform, metric.id)

        int level = (metric.level < 0) ? 0 : metric.level
        manager.setCell("")
        manager.setCell(metric.category)
        manager.setCell("")
        manager.setCell("")
        manager.setCell("")
        manager.setCell("")
        manager.setCell(platform)

        servers.each { String server ->
            manager.setCell("")
        }
    }

    void makeRowGroup(List<Integer> groupRows) {
        int groupRowStart = 1
        groupRows.each { groupRow ->
            int groupRowEnd = groupRow - 2 // エンドを2行前にセット
            if ((groupRow - groupRowStart) > 0) {
                manager.sheet.groupRow(groupRowStart, groupRowEnd)
            }
            groupRowStart = groupRow
        }
    }

    void make() {
        reportMaker.parseCellStyles("CellStyle")
        def resultSheetServerKeys = testScenario.resultSheetServerKeys.asMap()
        resultSheetServerKeys.each { String sheetName,
                                     Collection<String> refServers ->
            List<String> servers = refServers as List
            reportMaker.setTemplateSheet("TestResult")
            reportMaker.copyTemplate(sheetName)
            manager = reportMaker.createSheetManager()
            this.makeHeader(servers)

            manager.setPosition(1, 0)
            ResultSheet resultSheet = testScenario.getResultSheet(sheetName)
            int row = 1
            groupRows = new ArrayList<>()
            Metric metricOld
            ExcelConstants.RESULT_SHEET_PLATFORM_CATEGORY_ORDER.each {
                String platformCategory ->
                List<String> platforms = resultSheet.platforms.get(platformCategory)
                if (!platforms) {
                    return
                }
                platforms.each { String platform ->
                    testScenario.platformMetricKeys.get(platform).sort().each {
                        String metricKey ->
                        Metric metric = testScenario.metrics.get(metricKey)
                        metric.platform = platform // Fix metric.platform is null
                        if (metricOld && metric.category != metricOld.category) {
                            this.addCategoryRow(row, metricOld, servers)
                            row ++
                            groupRows << row
                        }
                        this.makeRow(row, metric, servers)
                        row ++
                        metricOld = metric
                    }
                }
            }
            this.addCategoryRow(row, metricOld, servers)
            row ++
            groupRows << row
            this.makeRowGroup(groupRows)
        }
    }

}