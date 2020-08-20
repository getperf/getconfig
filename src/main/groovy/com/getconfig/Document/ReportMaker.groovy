package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static com.getconfig.Document.ExcelConstants.*;

@TypeChecked
@CompileStatic
@Slf4j
class ReportMaker {
//    String FONT = "ＭＳ Ｐゴシック"
    String excelTemplate
    Workbook wb
    Sheet templateSheet
    Sheet sheet
    SheetManager sheetManager
    private Map<String,CellStyle> cellStyles

    ReportMaker(String excelTemplate) {
        this.excelTemplate = excelTemplate
    }

    ReportMaker read() {
        FileInputStream fis = new FileInputStream(this.excelTemplate)
        this.wb = (XSSFWorkbook)WorkbookFactory.create(fis)
        this.wb.createFont().setFontName(FONT)
        this.cellStyles = this.parseCellStyles(CELL_STYLE_SHEET_NAME)
        return this
    }

    void setTemplateSheet(String templateSheetName) {
        Sheet sheet = this.wb.getSheet(templateSheetName)
        if (!sheet) {
            String error = "not found sheet '${templateSheetName}' in ${excelTemplate}"
            throw new IllegalArgumentException(error)
        }
        this.templateSheet = sheet
    }

    void copyTemplate(String sheetName) {
        if (!this.templateSheet) {
            String error = "template sheet is not set"
            throw new IllegalArgumentException(error)
        }
        this.sheet = this.wb.cloneSheet(wb.getSheetIndex(this.templateSheet))
        this.wb.setSheetName(wb.getSheetIndex(this.sheet), sheetName)
    }

    SheetManager createSheetManager() {
        if (!this.templateSheet || !this.sheet) {
            String error = "template or copy target sheet is not set"
            throw new IllegalArgumentException(error)
        }
        this.sheetManager = new SheetManager(this.templateSheet, this.sheet)
        this.sheetManager.addCellStyles(this.cellStyles)
        this.sheetManager.setSheet(sheet)
        return this.sheetManager
    }

    /**
     * セルスタイルシートからコメント付きセルのセルスタイルを読込みセルスタイルの辞書を作成する
     */
    Map<String, CellStyle> parseCellStyles(String sheetName) {
        Sheet cellStyleSheet = this.wb.getSheet(sheetName)
        if (!cellStyleSheet) {
            String error = "not found cell style sheet : ${sheetName}"
            throw new IllegalArgumentException(error)
        }
        Map<String, CellStyle> cellStyles = new LinkedHashMap<>()
        for (Row cellStyleRow : cellStyleSheet) {
            for (Cell cellStyleCell : cellStyleRow) {
                Comment comment = cellStyleCell.getCellComment()
                if (comment) {
                    String headerId = comment.getString() as String
                    CellStyle newStyle = this.wb.createCellStyle()
                    newStyle.cloneStyleFrom(cellStyleCell.getCellStyle())
                    cellStyles.put(headerId, newStyle)
                }
            }
        }
        return cellStyles
    }

    /**
     * 編集後のシートの印刷設定、印刷範囲設定を行い終了処理をする
     */
    void finishSheetManager() {
        // 印刷設定、印刷範囲
        this.sheetManager.setPrintSetup(this.templateSheet.getPrintSetup())
        this.wb.setPrintArea(wb.getSheetIndex(sheet), this.sheetManager.getPrintArea())
    }

    void finish() {
        // テンプレートシートは不要なため削除
        [CELL_STYLE_SHEET_NAME, TEMPLATE_SUMMARY_SHEET_NAME, TEMPLATE_RESULT_SHEET_NAME,
         TEMPLATE_DEVICE_SHEET_NAME].each {sheetName ->
            int deleteSheetIndex = this.wb.getSheetIndex(sheetName)
            if (deleteSheetIndex != -1) {
                this.wb.removeSheetAt(deleteSheetIndex)
            }
        }
    }

    void write(String excelOutputPath) {
        // ファイル出力
        FileOutputStream fos = new FileOutputStream(excelOutputPath);
        wb.write(fos);
    }
}
