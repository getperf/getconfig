package com.getconfig.Document.SpecReader

import com.getconfig.Document.CellUtil
import com.getconfig.Document.ExcelConstants
import com.getconfig.Document.SpecReader
import com.getconfig.Model.PlatformParameter
import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.poiji.bind.Poiji
import com.poiji.option.PoijiOptions
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder
import com.poiji.exception.*

@Slf4j
@CompileStatic
class SpecReaderExcel implements SpecReaderBase {
    String inExcel = "getconfig.xlsx"
    protected Map<String, Integer> sheetIndexes = new LinkedHashMap<>()
    protected List<Server> testServers = new ArrayList<>()
    Map<String, PlatformParameter> platformParameters
            = new LinkedHashMap<>()

    Integer getTestServerSheetIndex() {
        return sheetIndexes.get(ExcelConstants.TEST_SERVER_SHEET_NAME)
    }

    String checkSheetNameContainsParameter(String sheetName) {
        String parameterName
        (sheetName=~/^${ExcelConstants.TEST_PARAMETER_SHEET_NAME_PREFIX}\((.+)\)$/).each { m0, m1 ->
            parameterName = m1
        }
        return parameterName
    }

    PlatformParameter parseParameterSheet(Sheet sheet) {
        PlatformParameter platformParameter = new PlatformParameter()
        for (int i=0; i <= sheet.getLastRowNum(); i++){
            Cell cell = sheet.getRow(i).getCell(0)
            Object value = CellUtil.getCellValue(cell)
            if (value) {
                platformParameter.add(value)
            }
        }
        return platformParameter
    }

    void readParameterSheet() throws FileNotFoundException {
        InputStream stream = new FileInputStream(new File(inExcel))
        Workbook wb = (XSSFWorkbook)WorkbookFactory.create(stream)
        for (int i=0; i<wb.getNumberOfSheets(); i++) {
            String sheetName = wb.getSheetName(i)
            String parameter = checkSheetNameContainsParameter(sheetName)
            if (parameter) {
                Sheet sheet = wb.getSheetAt(i)
                PlatformParameter platformParameter = parseParameterSheet(sheet)
                if (platformParameter) {
                    platformParameters.put(parameter, platformParameter)
                }
            }
            sheetIndexes.put(sheetName, i)
        }
        if (!(getTestServerSheetIndex())) {
            throw new IllegalArgumentException("test server sheet not found")
        }
    }

    List<Server> readServerSheet() {
        readParameterSheet()
        PoijiOptions options =PoijiOptionsBuilder.settings()
                .sheetIndex(getTestServerSheetIndex())
                .headerStart(ExcelConstants.TEST_SERVER_SHEET_HEADER_ROW)
                .build()
        InputStream stream = new FileInputStream(new File(this.inExcel))
        List<Server> servers = Poiji.fromExcel(stream, PoijiExcelType.XLSX,
                Server.class, options);
        return servers
    }

    ServerSpec read(String inFile) {
        this.inExcel = inFile
        readParameterSheet()
        this.testServers = readServerSheet()
        return new ServerSpec(this.testServers, this.platformParameters)
    }
}
