package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList;
import static com.getconfig.Document.ExcelConstants.*

@TypeChecked
@CompileStatic
@Slf4j
class SheetManager {
    public Sheet templateSheet;
    public Sheet sheet;
    private Row row;
    public Cell cell;
    private int offset;
    private int rowIndex;
    private int columnIndex;
    private Map<String,CellStyle> cellStyles = new LinkedHashMap<>();
    private Map<String,Integer> headers;

    /**
     * コンストラクタ
     * @param templateSheet 入力元テンプレートシート
     * @param sheet 出力先シート
     */
    SheetManager(Sheet templateSheet, Sheet sheet) {
        this.templateSheet = templateSheet
        this.setSheet(sheet)
    }

    /**
     * シートを設定し、参照するセル位置を「A1」に設定する
     * @param sheet
     */
    public SheetManager setSheet(Sheet sheet) {
        this.sheet = sheet;
        setPosition();
        this.offset = 1;
        this.rowIndex = 0;
        this.columnIndex = 0;

        return this
    }

    /**
     * シートの先頭行からコメントを読込み、ヘッダー辞書を作成する
     */
    Map<String,Integer> parseHeaderComment() {
        this.headers = new LinkedHashMap<>()
        Row headerRow = templateSheet.getRow(templateSheet.getFirstRowNum())
        for (Cell headerCell : headerRow) {
            Comment comment = headerCell.getCellComment()
            if (comment) {
                String headerId = comment.getString() as String
                this.headers.put(headerId, new Integer(headerCell.getColumnIndex()))
            }
        }
        return this.headers
    }

    /**
     * テンプレートの印刷設定をコピーする
     * @param printSetup
     */
    public void setPrintSetup(PrintSetup printSetup) {
        PrintSetup newSetup = this.sheet.getPrintSetup();

        // コピー枚数
        newSetup.setCopies(printSetup.getCopies());
        // 下書きモード
        //newSetup.setDraft(printSetup.getDraft());
        // シートに収まる高さのページ数
        newSetup.setFitHeight(printSetup.getFitHeight());
        // シートが収まる幅のページ数
        newSetup.setFitWidth(printSetup.getFitWidth());
        // フッター余白
        //newSetup.setFooterMargin(printSetup.getFooterMargin());
        // ヘッダー余白
        //newSetup.setHeaderMargin(printSetup.getHeaderMargin());
        // 水平解像度
        //newSetup.setHResolution(printSetup.getHResolution());
        // 横向きモード
        newSetup.setLandscape(printSetup.getLandscape());
        // 左から右への印刷順序
        //newSetup.setLeftToRight(printSetup.getLeftToRight());
        // 白黒
        newSetup.setNoColor(printSetup.getNoColor());
        // 向き
        newSetup.setNoOrientation(printSetup.getNoOrientation());
        // 印刷メモ
        //newSetup.setNotes(printSetup.getNotes());
        // ページの開始
        //newSetup.setPageStart(printSetup.getPageStart());
        // 用紙サイズ
        newSetup.setPaperSize(printSetup.getPaperSize());
        // スケール
        newSetup.setScale(printSetup.getScale());
        // 使用ページ番号
        //newSetup.setUsePage(printSetup.getUsePage());
        // 有効な設定
        //newSetup.setValidSettings(printSetup.getValidSettings());
        // 垂直解像度
        //newSetup.setVResolution(printSetup.getVResolution());
    }

    /**
     * 印刷範囲取得
     * @return
     */
    public String getPrintArea() {
        int firstRow = this.sheet.getFirstRowNum();
        int lastRow = this.rowIndex;
        int firstColumn = this.sheet.getRow(firstRow).getFirstCellNum();
        int lastColumn = this.sheet.getRow(lastRow).getLastCellNum()-1;

        return new CellRangeAddress(
                firstRow, lastRow, firstColumn, lastColumn).formatAsString()
    }

    // 移動処理

    /**
     * 参照する行位置を設定する
     */
    private void setRowPosition() {
        if((this.row = sheet.getRow(this.rowIndex)) == null) {
            this.row = sheet.createRow(this.rowIndex);
        }
    }

    /**
     * 参照するセル位置を設定する
     */
    private void setCellPosition() {
        if((this.cell = row.getCell(this.columnIndex)) == null) {
            this.cell = row.createCell(this.columnIndex);
        }
    }

    /**
     * オフセット（移動量）を設定する
     * ※デフォルトは「1」
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 参照する行位置、セル位置を設定する
     */
    public void setPosition() {
        setRowPosition();
        setCellPosition();
    }

    /**
     * 行と列を指定して参照するセル位置を設定する
     * @param rowIndex
     *          行位置(0ベース)
     * @param columnIndex
     *          列位置(0ベース)
     */
    public void setPosition(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        setPosition();
    }

    /**
     * 設定してあるオフセットに従って参照するセル位置を移動する
     * 現在列位置+オフセット
     */
    public void nextCell() {
        moveCell(this.offset);
    }

    /**
     * 指定したオフセットに従って参照するセル位置を移動する
     * 現在列位置+オフセット
     * @param columnOffset
     */
    public void moveCell(int columnOffset) {
        move(0, columnOffset);
    }

    /**
     * 設定してあるオフセットに従って参照する行位置を移動する
     * 現在行位置+オフセット
     * ※列位置は0（A）列になる
     */
    public void nextRow() {
        nextRow(0);
    }

    /**
     * 設定してあるオフセットに従って参照する行位置を移動し、指定した列位置に移動する
     * 現在行位置+オフセット
     * @param columnIndex
     */
    public void nextRow(int columnIndex) {
        this.columnIndex = columnIndex;
        moveRow(this.offset);
    }

    /**
     * 指定したオフセットに従って参照する行位置を移動する
     * 現在行位置+オフセット
     * ※列位置は変わらない
     * @param rowOffset
     */
    public void moveRow(int rowOffset) {
        move(rowOffset, 0);
    }

    /**
     * 指定した行オフセット、列オフセットに従って参照する行位置、列位置を移動する
     * 現在行位置+行オフセット
     * 現在列位置+列オフセット
     * @param rowOffset
     * @param columnOffset
     */
    public void move(int rowOffset, int columnOffset) {
        this.rowIndex += rowOffset;
        this.columnIndex += columnOffset;
        setPosition();
    }

    /**
     * 現在行位置から以降の行を1行下へシフトする（行の挿入）
     * ※上の行のスタイル、高さを引き継ぐ
     */
    public void shiftRows() {
        int lastRowNum = this.sheet.getLastRowNum();
        int lastCellNum = this.sheet.getRow(this.rowIndex-1).getLastCellNum();

        this.sheet.shiftRows(this.rowIndex, lastRowNum+1, 1);
        Row newRow = this.sheet.getRow(this.rowIndex);
        if(newRow == null) {
            newRow = this.sheet.createRow(this.rowIndex);
        }
        Row oldRow = this.sheet.getRow(this.rowIndex-1);
        // for(int i = 0; i < lastCellNum-1; i++) {
        for(int i = 0; i < lastCellNum; i++) {
            Cell newCell = newRow.createCell(i);
            Cell oldCell = oldRow.getCell(i);
            // oldCellがnullでなければスタイル設定
            // wrokbookに作成出来るcellstyleには上限があるのでmapに詰める
            if(oldCell != null) {
                String columnId = "${this.sheet.sheetName}_${i}"
                CellStyle cellStyle = cellStyles.get(columnId)
                if (!cellStyle) {
                    cellStyle = this.sheet.getWorkbook().createCellStyle();
                    cellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    cellStyles.put(columnId, cellStyle);
                }
                newCell.setCellStyle(cellStyle);
            }
        }
        newRow.setHeightInPoints(oldRow.getHeightInPoints());
        setPosition();
    }

    /**
     * String型の値をセルに設定し、指定したオフセットに従って参照する列位置を移動する
     * 値がなければBLANKを設定する
     * @param value
     * @param columnOffset
     */
    public void setCellValue(String value, int columnOffset) {
        if(!value || value.trim().length() == 0) {
            this.cell.setBlank();
        }else {
            this.cell.setCellValue(value);
        }
        moveCell(columnOffset);
    }

    /**
     * String型の値をセルに設定し、設定してあるオフセットに従って参照する列位置を移動する
     * 値がなければBLANKを設定する
     * @param value
     */
    public void setCellValue(String value) {
        setCellValue(value, this.offset);
    }

    /**
     * int型の値をセルに設定し、指定したオフセットに従って参照する列位置を移動する
     * @param value
     * @param columnOffset
     */
    public void setCellValue(int value, int columnOffset) {
        this.cell.setCellValue(value);
        moveCell(columnOffset);
    }

    /**
     * int型の値をセルに設定し、設定してあるオフセットに従って参照する列位置を移動する
     * @param value
     */
    public void setCellValue(int value) {
        setCellValue(value, this.offset);
    }

    /**
     * double型の値をセルに設定し、指定したオフセットに従って参照する列位置を移動する
     * @param value
     * @param columnOffset
     */
    public void setCellValue(double value, int columnOffset) {
        this.cell.setCellValue(value);
        moveCell(columnOffset);
    }

    /**
     * double型の値をセルに設定し、設定してあるオフセットに従って参照する列位置を移動する
     * @param value
     */
    public void setCellValue(double value) {
        setCellValue(value, this.offset);
    }

    /**
     * double型の値をセルに設定し、設定してあるオフセットに従って参照する列位置を移動する
     * @param style id
     */
    public void setCellStyle(String cellStyleId = DEFAULT_CELL_STYLE_ID) {
        CellStyle cellStyle = this.cellStyles.get(cellStyleId) ?: this.cellStyles.get(DEFAULT_CELL_STYLE_ID)
        if (cellStyle) {
            this.cell.setCellStyle(cellStyle)
        }
    }

    public setCell(Object value, String cellStyleId = DEFAULT_CELL_STYLE_ID) {
        if (value instanceof Long) {
            this.setCell((double)value, "Integer")
        } else if (value instanceof Integer) {
            this.setCell((int)value, "Integer")
        } else if (value instanceof Double || value instanceof Float || 
            value instanceof BigDecimal) {
//            this.setCell((double)value, "Double")
            this.setCell(value as Double, "Double")
        } else {
            this.setCell(value as String)
        }
        // if (value instanceof Long) {
        //     this.setCell((double)value, "Integer")
        // } else if (value instanceof Integer) {
        //     this.setCell((int)value as Integer, "Integer")
        // } else if (value instanceof Double || value instanceof Float || 
        //     value instanceof BigDecimal) {
        //     this.setCell(value as Double, "Double")
        // } else {
        //     this.setCell(value as String)
        // }
        // println value instanceof Integer
        // println value instanceof Double

        // this.setCellStyle(cellStyleId)
        // this.setCellValue(value as String)
    }

    public setCell(String value, String cellStyleId = DEFAULT_CELL_STYLE_ID) {
        this.setCellStyle(cellStyleId)
        this.setCellValue(value)
    }

    public setCell(int value, String cellStyleId = DEFAULT_CELL_STYLE_ID) {
        this.setCellStyle(cellStyleId)
        this.setCellValue(value)
    }

    public setCell(double value, String cellStyleId = DEFAULT_CELL_STYLE_ID) {
        this.setCellStyle(cellStyleId)
        this.setCellValue(value)
    }

    public void addCellStyles(Map<String, CellStyle> cellStyles) {
        this.cellStyles.putAll(cellStyles)
    }
}
