package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil

@Slf4j
@TypeChecked
@CompileStatic
class CellUtil {
    static Object getCellValue(Cell cell) {
        Objects.requireNonNull(cell, "cell is null");

        CellType cellType = cell.getCellType()
        if (cellType == CellType.BLANK) {
            return null;
        } else if (cellType == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cellType == CellType.FORMULA) {
            throw new IllegalArgumentException("not support formula cell type");
        } else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        } else if (cellType == CellType.STRING) {
            return cell.getStringCellValue();
        } else {
            throw new IllegalArgumentException("not support ${cellType}");
        }
    }
}
