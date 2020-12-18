package com.getconfig.Document.SpecReader

import com.getconfig.Model.PlatformParameter
import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
@ToString
class ServerSpec {
    List<Server> testServers
    Map<String, PlatformParameter> platformParameters

    ServerSpec(List<Server> testServers,
               Map<String, PlatformParameter> platformParameters) {
        this.testServers = testServers
        this.platformParameters = platformParameters
    }
}
