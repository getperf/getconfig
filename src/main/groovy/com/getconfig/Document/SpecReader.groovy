package com.getconfig.Document

import com.getconfig.ConfigEnv
import com.getconfig.Document.SpecReader.ServerSpec
import com.getconfig.Document.SpecReader.SpecReaderBase
import com.getconfig.Document.SpecReader.SpecReaderExcel
import com.getconfig.Document.SpecReader.SpecReaderToml
import com.getconfig.Model.PlatformParameter
import com.getconfig.Model.Server
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.apache.commons.io.FilenameUtils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic 
public class SpecReader {
    String inExcel = "getconfig.xlsx"
    ServerSpec serverSpec

    int serverCount() {
        return serverSpec.testServers.size()
    }

    List<Server> testServers() {
        return serverSpec.testServers
    }

    Map<String, PlatformParameter> platformParameters() {
        return serverSpec.platformParameters
    }

    void mergeConfig() {
        def configEnv = ConfigEnv.instance
        List<Server> servers = new ArrayList<Server>()
        serverSpec.testServers.each { server ->
            configEnv.setAccount(server)
            if (server.validate()) {
                servers << server
            }
        }
        serverSpec.testServers = servers
    }

    List<Server> normalizeServers(List<Server> testServers) {
        String previousServerName
        String previousCompareServer
        Multimap<String, String> comparedServers = HashMultimap.create()
        List<Server> normalizedServers = new ArrayList<>()

        testServers.each { server ->
            server.initDomain()
            // サーバー名があり、比較対象がない行は、比較対象なしとして前行の比較対象をリセットする
            if (server.serverName && !(server.compareServer)) {
                previousCompareServer = null
            }
            // サーバ名、比較対象が空の場合、前行の値をセットする
            if (server.checkKey(previousServerName, previousCompareServer)) {
                normalizedServers << server
                comparedServers.put(server.serverName, server.domain)
                previousServerName = server.serverName
                previousCompareServer = server.compareServer
            }
        }
        // println "NORMALIZED:"
        // println normalizedServers*.serverName
        // println normalizedServers*.domain
        // println normalizedServers*.compareServer

        List<Server> comparedOptionServers = new ArrayList<>()
        normalizedServers.each { server ->
            String compareServer = server.compareServer
            if (compareServer) {
                if (!(comparedServers.containsEntry(compareServer, server.domain))) {
                    log.info "clone '${compareServer}' for ${server.serverName}, ${server.domain} test"
                    Server addedServer = server.cloneComparedServer(compareServer)
                    comparedServers.put(compareServer, server.domain)
                    comparedOptionServers << addedServer
                }
            }
        }
        // println "COMPAREDOPTIONSERVERS:"
        // println comparedOptionServers*.serverName
        // println comparedOptionServers*.domain
        // println comparedOptionServers*.compareServer
        normalizedServers.addAll(comparedOptionServers)
        return normalizedServers
    }

    void parse() throws FileNotFoundException, IllegalArgumentException {
        SpecReaderBase specReader
        switch(FilenameUtils.getExtension(inExcel)) {
            case 'xlsx':
                specReader = new SpecReaderExcel()
                break
            case 'toml':
                specReader = new SpecReaderToml()
                break
            default:
                throw new IllegalArgumentException("not support extension ${inExcel}")
                break
        }
//        SpecReaderBase specReader = new SpecReaderExcel()
        serverSpec = specReader.read(inExcel)
        List<Server> normalizedServer = normalizeServers(
                                            serverSpec.testServers)
        serverSpec.testServers = normalizedServer
    }

    void print() {
        println this.serverCount()
        this.testServers().each { server ->
            println server
        }
    }

    void run() {
        this.parse()
        this.mergeConfig()
    }

}
