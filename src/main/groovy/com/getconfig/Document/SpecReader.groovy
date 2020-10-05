package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import com.poiji.bind.Poiji
import com.poiji.option.PoijiOptions
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder
import com.poiji.exception.*
import com.getconfig.Model.Server
import com.getconfig.ConfigEnv

@Slf4j
@CompileStatic 
public class SpecReader {
    static final String ExcelFilename = "サーバチェックシート.xlsx"
    static final int SpecSheetIndex = 1
    static final int SpecHeaderRow = 2

    String inExcel = ExcelFilename // "./サーバチェックシート.xlsx"
    protected List<Server> testServers = new ArrayList<Server>()

    void parse() throws FileNotFoundException {
        PoijiOptions options =PoijiOptionsBuilder.settings()
                  .sheetIndex(SpecSheetIndex)
                  .headerStart(SpecHeaderRow)
                  .build()
        InputStream stream = new FileInputStream(new File(inExcel))
        List<Server> servers = Poiji.fromExcel(stream, PoijiExcelType.XLSX,
                  Server.class, options);

        String previousServerName
        servers.each { server ->
            if (server.checkKey(previousServerName)) {
                this.testServers << server
                previousServerName = server.serverName
            }
        }
    }

    int serverCount() {
        return this.testServers.size()
    }

    List<Server> testServers() {
        return this.testServers
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
