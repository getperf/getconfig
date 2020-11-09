package com.getconfig.Document

import com.getconfig.Model.PlatformParameter
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
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
import com.getconfig.Model.Server
import com.getconfig.ConfigEnv

@Slf4j
@CompileStatic 
public class SpecReader {
    String inExcel = "getconfig.xlsx"
    protected Map<String, Integer> sheetIndexes = new LinkedHashMap<>()
    protected List<Server> testServers = new ArrayList<>()
    Map<String, PlatformParameter> platformParameters
            = new LinkedHashMap<>()

    int serverCount() {
        return this.testServers.size()
    }

    List<Server> testServers() {
        return this.testServers
    }

    Map<String, PlatformParameter> platformParameters() {
        return this.platformParameters
    }

    void mergeConfig() {
        def configEnv = ConfigEnv.instance
        List<Server> servers = new ArrayList<Server>()
        this.testServers.each { server ->
            configEnv.setAccount(server)
            if (server.validate()) {
                servers << server
            }
        }
        this.testServers = servers
    }

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

    void parse() throws FileNotFoundException, IllegalArgumentException {
        readParameterSheet()
        PoijiOptions options =PoijiOptionsBuilder.settings()
                  .sheetIndex(getTestServerSheetIndex())
                  .headerStart(ExcelConstants.TEST_SERVER_SHEET_HEADER_ROW)
                  .build()
        InputStream stream = new FileInputStream(new File(inExcel))
        List<Server> servers = Poiji.fromExcel(stream, PoijiExcelType.XLSX,
                  Server.class, options);
        String previousServerName
        String previousCompareServer
        Multimap<String, String> comparedServers = HashMultimap.create()
        servers.each { server ->
            // サーバー名があり、比較対象がない行は、比較対象なしとして前行の比較対象をリセットする
            if (server.serverName && !(server.compareServer)) {
                previousCompareServer = null
            }
            // サーバ名、比較対象が空の場合、前行の値をセットする
            if (server.checkKey(previousServerName, previousCompareServer)) {
                this.testServers << server
                comparedServers.put(server.serverName, server.domain)
                previousServerName = server.serverName
                previousCompareServer = server.compareServer
            }
        }
        servers.each { server ->
            String compareServer = server.compareServer
            if (compareServer) {
                if (!(comparedServers.containsEntry(compareServer, server.domain))) {
                    log.info "clone '${compareServer}' for ${server.serverName}, ${server.domain} test"
                    Server addedServer = server.cloneComparedServer(compareServer)
                    comparedServers.put(compareServer, server.domain)
                    this.testServers << addedServer
                }
            }
        }
    }

    void print() {
        println this.sheetIndexes;
        println this.testServers.size();
        this.testServers.each { server ->
            println server
        }
    }

    void run() {
        this.parse()
        this.mergeConfig()
    }
}
