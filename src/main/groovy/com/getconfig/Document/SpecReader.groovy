package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import com.poiji.bind.Poiji
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import org.apache.poi.ss.usermodel.*
import com.poiji.option.PoijiOptions
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder
import com.poiji.exception.*
import com.poiji.annotation.ExcelCell
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic 
public class SpecReader {
    static final String ExcelFilename = "サーバチェックシート.xlsx"
    static final int SpecSheetIndex = 1
    static final int SpecHeaderRow = 2

    String inExcel = ExcelFilename // "./サーバチェックシート.xlsx"
    protected List<TestServer> testServers = new ArrayList<TestServer>()

    void parse() throws FileNotFoundException {
        PoijiOptions options =PoijiOptionsBuilder.settings()
                  .sheetIndex(SpecSheetIndex)
                  .headerStart(SpecHeaderRow)
                  .build()
        InputStream stream = new FileInputStream(new File(inExcel))
        List<TestServer> servers = Poiji.fromExcel(stream, PoijiExcelType.XLSX, 
                  TestServer.class, options);

        servers.each { server ->
            if (server.validate() == 1) {
                log.info "server : ${server}"
                this.testServers << server
            }
        }
    }

    int serverCount() {
        return this.testServers.size()
    }

    void print() {
        println this.testServers.size();
        this.testServers.each { server ->
            println server
        }
        // // 3
        // TestServer firstServer = servers.get(0);
        // println firstServer;
    }

    void run() {
        this.parse()
        this.print()
    }
}
