package com.getconfig.Document

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
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
    static final String ExcelFilename = "getconfig.xlsx"
    static final int SpecSheetIndex = 1
    static final int SpecHeaderRow = 2

    String inExcel = ExcelFilename // "./getconfig.xlsx"
    protected List<Server> testServers = new ArrayList<>()

    void parse() throws FileNotFoundException {
        PoijiOptions options =PoijiOptionsBuilder.settings()
                  .sheetIndex(SpecSheetIndex)
                  .headerStart(SpecHeaderRow)
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
