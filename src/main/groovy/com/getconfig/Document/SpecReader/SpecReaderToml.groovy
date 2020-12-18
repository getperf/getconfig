package com.getconfig.Document.SpecReader

import com.getconfig.Model.Server
import com.getconfig.Utils.TomlUtils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SpecReaderToml implements SpecReaderBase {

    ServerSpec read(String inFile) {
        ServerSpec serverSpec = TomlUtils.read(inFile, ServerSpec) as ServerSpec
        int order = 1
        serverSpec.testServers.each { Server server ->
            server.order = order
            order ++
        }
        return serverSpec
    }
}
