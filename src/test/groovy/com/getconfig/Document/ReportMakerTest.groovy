package com.getconfig.Document

import com.getconfig.Model.PlatformMetric
import com.getconfig.Utils.TomlUtils
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.common.usermodel.Hyperlink
import spock.lang.Specification

class ReportMakerTest extends Specification {
    String excelTemplate = "lib/template/report_summary.xlsx"

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
        println reportMaker.cellStyles.keySet()
        1 == 1
    }

    def "検査結果プロトタイプ"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.parseCellStyles("CellStyle")
        reportMaker.setTemplateSheet("TestResult")
        reportMaker.copyTemplate("検査結果")
        SheetManager manager = reportMaker.createSheetManager()

        PlatformMetric metrics = TomlUtils.read("lib/dictionary/Linux.toml",
                                                PlatformMetric)
        metrics.platform = 'Linux'
        metrics.validate()
        manager.nextRow()
        metrics.getAll().each { metric ->
            println metric
            manager.setCellValue(metric.level)
            manager.setCellValue(metric.category)
            manager.setCellValue(metric.name)
            manager.setCellValue(metric.id)
            manager.setCellValue(metric.deviceFlag.toString())
            manager.setCellValue(metric.comment)
            manager.setCellValue(metric.platform)
            manager.nextRow()
            manager.shiftRows()
        }

        reportMaker.finish()
        reportMaker.write("build/report4.xlsx")

        then:
        1 == 1
    }

    def "リンク作成"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.setTemplateSheet("Summary")
        reportMaker.copyTemplate("検査シート")
        SheetManager manager = reportMaker.createSheetManager()

        Workbook wb = reportMaker.wb
        CreationHelper ch = wb.getCreationHelper()

        Row row = reportMaker.sheet.createRow(10);

        Hyperlink link = ch.createHyperlink(HyperlinkType.DOCUMENT)
        link.setAddress("デバイス!A1")
        Cell cell = row.createCell(1);
        cell.setCellValue("abc")
        cell.setHyperlink(link)

        manager.move(11,1)
        manager.cell.setHyperlink(link)
        println manager.cell.getAddress()
        manager.setCell("def", "Link")
        // cell.setHyperlink()は、String 型のアドレスを指定する必要がある
        // 詳細シートへのリンクの場合、シート作成時にどこのシートのアドレスの指定は困難。
        // 詳細シート作成時にリンクキー：アドレスの辞書を作成し、その後にサマリシート
        // に戻ってリンクを設定する

        CellStyle style = wb.createCellStyle()
        Font font = wb.createFont()
        font.setUnderline(Font.U_SINGLE)
        style.setFont(font)
        cell.setCellStyle(style)

        reportMaker.setTemplateSheet("Device")
        reportMaker.copyTemplate("デバイス")

        reportMaker.finish()
        reportMaker.write("build/report5.xlsx")

        then:
        new File("build/report5.xlsx").exists() == true
    }

    def "リンク作成2"() {
        when:
        ReportMaker reportMaker = new ReportMaker(excelTemplate).read()
        reportMaker.setTemplateSheet("TestResult")
        reportMaker.copyTemplate("検査結果")
        SheetManager manager = reportMaker.createSheetManager()

        Workbook wb = reportMaker.wb
        CreationHelper ch = wb.getCreationHelper()
        manager.move(1,4)
        // manager.cell.setHyperlink(link)
        println manager.cell.getAddress()
        reportMaker.setDetailLink("デバイス", manager.cell, "A1")
        manager.setCell("詳細", "Link")

        reportMaker.setTemplateSheet("Device")
        reportMaker.copyTemplate("デバイス")
        manager = reportMaker.createSheetManager()
        manager.move(3,0)
        Map<String, Hyperlink> links = reportMaker.getBackLinks("デバイス")
        links.each {String name, Hyperlink link ->
            manager.cell.setHyperlink(link)
            manager.setCell("${name}に戻る", "LinkNoFrame")
        }

        reportMaker.finish()
        reportMaker.write("build/report6.xlsx")

        then:
        new File("build/report5.xlsx").exists() == true
    }
}
