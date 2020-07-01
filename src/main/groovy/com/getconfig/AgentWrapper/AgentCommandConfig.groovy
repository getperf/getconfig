package com.getconfig.AgentWrapper

import groovy.transform.*

@ToString
@TypeChecked
@CompileStatic
class AgentCommandConfig {
    String server
    boolean local_exec
//    List<AgentCommandServerConfig>
    def servers = new ArrayList<ServerConfig>()
}
