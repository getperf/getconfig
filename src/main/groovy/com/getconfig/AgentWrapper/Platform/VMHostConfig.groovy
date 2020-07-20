package com.getconfig.AgentWrapper.Platform

import com.getconfig.AgentWrapper.AgentCommandConfig
import groovy.transform.*

@ToString
@TypeChecked
@CompileStatic
class VMHostConfig {
    String server
    String url
    String user
    String password
    boolean local_exec = false

    List<String> servers = new ArrayList<String>()
}
