package com.getconfig.Document

import spock.lang.Specification
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class SheetManagerTest extends Specification {
    String FILE_PATH_TEMPLATE = 'lib/template/report_summary.xlsx'
    String FILE_PATH_OUTPUT = 'build/report.xlsx'
    Workbook wb
    Sheet templateSheet
    Sheet sheet

    def setup() {
        FileInputStream fis = new FileInputStream(FILE_PATH_TEMPLATE);
        wb = (XSSFWorkbook)WorkbookFactory.create(fis);
        templateSheet = this.wb.getSheet('Summary')
        sheet = wb.cloneSheet(wb.getSheetIndex(this.templateSheet))
    }

    def "セル範囲指定"() {
        when:
        SheetManager manager = new SheetManager(templateSheet, sheet)

        then:
        manager.getPrintArea() == "A1:AA1"
    }

    def "ヘッダコメント取得"() {
        when:
        templateSheet = wb.getSheet('Summary')
        sheet = wb.cloneSheet(wb.getSheetIndex(templateSheet))
        SheetManager manager = new SheetManager(templateSheet, sheet)
        manager.parseHeaderComment()

        then:
        manager.headers.size() > 0
    }

    def "検査結果ヘッダコメント取得"() {
        when:
        templateSheet = wb.getSheet('TestResult')
        sheet = wb.cloneSheet(wb.getSheetIndex(templateSheet))
        SheetManager manager = new SheetManager(templateSheet, sheet)
        manager.parseHeaderComment()

        then:
        manager.headers.size() > 0
    }
}
