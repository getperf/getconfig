package com.getconfig.GconfWrapper

import groovy.transform.*

@ToString
@CompileStatic
class GconfConfig {
    String server
    boolean local_exec
    List<GconfServer> servers = new ArrayList<GconfServer>()
}
