package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import com.poiji.bind.Poiji
import com.poiji.option.PoijiOptions
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder
import com.poiji.exception.*
import com.getconfig.Model.TestServer
import com.getconfig.ConfigEnv

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
            if (server.checkKey()) {
                this.testServers << server
            }
        }
    }

    int serverCount() {
        return this.testServers.size()
    }

    List<TestServer> testServers() {
        return this.testServers
    }

    void mergeConfig() {
        def configEnv = ConfigEnv.instance
        List<TestServer> servers = new ArrayList<TestServer>()
        this.testServers.each { server ->
            configEnv.setAccount(server)
            if (server.validate()) {
                servers << server
            }
        }
        this.testServers = servers
    }

    void print() {
        println this.testServers.size();
        this.testServers.each { server ->
            println server
        }
    }

    void run() {
        this.parse()
        this.mergeConfig()
        this.print()
    }
}
