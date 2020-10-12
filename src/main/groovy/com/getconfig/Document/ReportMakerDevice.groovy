package com.getconfig.Document


import com.getconfig.Model.ResultLine
import com.getconfig.Model.TestScenario
import com.google.common.collect.*
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.Hyperlink

@Slf4j
@TypeChecked
@CompileStatic
class ReportMakerDevice {
    TestScenario testScenario
    ReportMaker reportMaker

    ReportMakerDevice(TestScenario testScenario, ReportMaker reportMaker) {
        this.testScenario = testScenario
        this.reportMaker = reportMaker
    }

    void make() {
        reportMaker.setTemplateSheet("Device")
        Table<String, String, ResultLine> devices = this.testScenario.devices
        SheetManager manager
        devices.columnKeySet().each {String sheetName ->
            List<String> headers
            int columnSize = 0
            int row = 0
            devices.column(sheetName).reverseEach {
                String server, ResultLine device ->
                if (device.csv.size() == 0) {
                    return
                }
                if (!headers) {
                    reportMaker.copyTemplate(sheetName)
                    manager = reportMaker.createSheetManager()
                    headers = device.headers as List<String>
                    columnSize = headers.size()
                    int columnPosition = 1
                    manager.setPosition(0,columnPosition)
                    headers.each { String header ->
                        manager.setCell(header, "HeaderServer")
                        columnPosition ++
                    }
                }
                device.csv.each { line ->
                    int column = 0
                    row ++
                    manager.setPosition(row,0)
                    manager.setCell(server, "Normal")
                    column ++
                    line.each { value ->
                        manager.setCell(value as String, "Normal")
                        column ++
                    }
                    while (column <= columnSize) {
                        manager.setCell("", "Normal")
                        column ++
                    }
                }
            }
            if (columnSize > 0) {
                (0..columnSize).each { columnPosition ->
                    manager.sheet.autoSizeColumn(columnPosition as int)
                }

                Map<String, Hyperlink> links = reportMaker.getBackLinks(sheetName)
                links.sort().reverseEach {String backSheetName, Hyperlink link ->
                    manager.setPosition(row + 2, 0)
                    manager.cell.setHyperlink(link)
//                    manager.setCell("${backSheetName}に戻る", "LinkNoFrame")
                    manager.setCell(backSheetName, "LinkNoFrame")
                    row ++
                }
            }
        }
    }
}
