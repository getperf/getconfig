package com.getconfig.AgentWrapper

import groovy.transform.*

@ToString
@CompileStatic
class ServerConfig {
    String  server
    String  url
    String  user
    String  password
    String  ssh_key
    boolean insecure
}
