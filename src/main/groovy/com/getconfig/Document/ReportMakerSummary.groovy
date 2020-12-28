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

//    void resetHeader(SheetManager manager) {
//        Sheet sheet = manager.sheet
//        Row headerRow = sheet.getRow(sheet.getFirstRowNum())
//        // ヘッダーのコメントを削除。
//        // headerCell.removeCellComment() の　NullPointer Exception を回避するため、
//        // A2 列にダミーの comment を作成する
//        final Drawing drawing = sheet.createDrawingPatriarch();
//        final ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 1, 0, 1);
//        final Comment dummyComment = drawing.createCellComment(anchor)
//        for (Cell headerCell : (headerRow as List<Cell>)) {
//            Comment comment = headerCell.getCellComment()
//            if (headerCell && comment) {
//                headerCell.removeCellComment()
//            }
//        }
//        // ヘッダーにオートフィルター設定
//        sheet.setAutoFilter(new CellRangeAddress(
//                0,sheet.getLastRowNum(), 0, headerRow.size() - 1))
//    }

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
        manager.resetHeader(0)
    }
}
