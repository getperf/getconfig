package com.getconfig.Document

import org.apache.poi.ss.usermodel.CellStyle
import spock.lang.Specification

class ReportMakerTest extends Specification {
    String excelTemplate = "lib/template/summary_report.xlsx"

    def "テンプレート読込み"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.setTemplateSheet("Summary")
        reportMaker.copyTemplate("検査シート")
        reportMaker.write("build/report2.xlsx")

        then:
        new File("build/report2.xlsx").exists() == true
    }

    def "レポート作成"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()

        reportMaker.setTemplateSheet("Summary")
        reportMaker.copyTemplate("サマリレポート")
        SheetManager manager = reportMaker.createSheetManager()

        // この間でシートの編集操作を実行
        manager.nextRow()
        manager.setCellValue(1)
        manager.setCellValue("ostrich")
        manager.setCellValue("Linux")
        manager.setCellValue("192.168.10.1")
        manager.nextRow()
        manager.shiftRows()
        manager.setCellValue(2)
        manager.setCellValue("centos80")

        reportMaker.finishSheetManager()

        reportMaker.write("build/report2.xlsx")

        then:
        1 == 1
    }

    def "セルスタイル読込み"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.parseCellStyles("CellStyle")
        reportMaker.setTemplateSheet("TestResult")
        reportMaker.copyTemplate("検査結果")
        SheetManager manager = reportMaker.createSheetManager()

        manager.move(0, 7)
        manager.setCell("ostrich", "HeaderServer")
        manager.setCell("TagResult", "HeaderTag")

        manager.move(1, -2)
        manager.setCell("cent80", "Normal")
        manager.setCell(1.0, "Match")

        manager.move(1, -2)
        manager.shiftRows()
        manager.setCell("not found", "NotFound")
        manager.setCell(0.5, "Unmatch")

        manager.move(1, -2)
        manager.shiftRows()
        manager.setCell("same", "SameAs")
        manager.setCell(2.0, "Match")

        reportMaker.finish()
        reportMaker.write("build/report3.xlsx")

        then:
        println reportMaker.cellStyles
        1 == 1
    }

}