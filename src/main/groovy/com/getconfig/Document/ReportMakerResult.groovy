package com.getconfig.Document

import com.getconfig.Model.Metric
import com.getconfig.Model.MetricId
import com.getconfig.Model.ReportResult
import com.getconfig.Model.Result
import com.getconfig.Model.ResultSheet
import com.getconfig.Model.ResultTag
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
    SheetManager manager
    List<Integer> groupRows
    String tagExists

    ReportMakerResult(TestScenario testScenario, ReportMaker reportMaker) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
    }

    List<String> reOrderServersByTag(List<String> servers) {
        List<String> orderedServers = new ArrayList<>()
        int tagIndex = 0
        servers.each { String server ->
            if (testScenario.checkTagServer(server)) {
                orderedServers.add(tagIndex, server)
                tagIndex ++
            }else {
                orderedServers.add(server)
            }
        }
        return orderedServers
    }

    void makeHeader(List<String> servers) {
        int columnPosition = 7
        Map<String, ReportResult.ReportColumn> resultColumns =
                testScenario.reportResult.getColumns()
        Map<String, Integer>headers = manager.parseHeaderComment()
        def headerNames = headers.sort { a, b ->
            a.value <=> b.value }.keySet()
        manager.setPosition(0,0)
        headerNames.each {String headerName ->
            String headerLabel = resultColumns.get(headerName)?.getName() ?: headerName
            manager.setCell(headerLabel, "HeaderServer")
        }

        manager.sheet.setColumnHidden(5, true)
        manager.setPosition(0, columnPosition)
        tagExists = null
        servers.each { String server ->
            String compareServer = testScenario.getServerGroupTag(server)
            String suffix = (compareServer) ? ":${compareServer}" : ""
            manager.setCell(server+suffix, "HeaderServer")
            manager.sheet.setColumnWidth(columnPosition, 48*256)
            if (testScenario.checkTagServer(server)) {
                tagExists = server
            }
            columnPosition ++
        }
        if (tagExists) {
            manager.setCell("TAG" as String, "HeaderServer")
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
                // manager.setCell(result.value)
                if (result.comparison == Result.ResultStatus.MATCH) {
                    manager.setCell("same as", "SameAs")
                } else {
                    manager.setCell(result.value)
                }
            } else {
                manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
            }
        }
        if (tagExists) {
            ResultTag resultTag = testScenario.resultTags.get(tagExists, metricId)
            // if (resultTag && resultTag.used() == false) {
            //     manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
            if (!resultTag || !(resultTag.used())) {
                manager.setCell(ExcelConstants.CELL_NOT_UNKOWN_VALUE, "Null")
            } else {
                double rate = (resultTag) ? resultTag.rate() : 0
                manager.setCell(rate, (rate == 1.0) ? "Match": "UnMatch")
            }
        }
    }

    void addCategorySummaryRow(int row, Metric metric, List<String> servers) {
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
        if (tagExists) {
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

    int columnSize(int serverCount) {
        int column = 6
        column += serverCount
        if (tagExists) {
            column ++
        }
        return column
    }

    void make() {
        reportMaker.parseCellStyles("CellStyle")
        def resultSheetServerKeys = testScenario.resultSheetServerKeys.asMap()
        int dummyAnchorIndex
        resultSheetServerKeys.each { String sheetName,
                                     Collection<String> refServers ->
            List<String> servers = this.reOrderServersByTag(refServers as List)
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
                            this.addCategorySummaryRow(row, metricOld, servers)
                            row ++
                            groupRows << row
                        }
                        this.makeRow(row, metric, servers)
                        row ++
                        metricOld = metric
                    }
                }
            }
            this.addCategorySummaryRow(row, metricOld, servers)
            row ++
            groupRows << row
            this.makeRowGroup(groupRows)
            int column = this.columnSize(servers.size())
            // dummyAnchorIndex ++
            // manager.removeHeaderComment(dummyAnchorIndex)
            manager.setHeaderAutoFilter(column, row)
        }
    }

}