package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ServerPortList {
    String serverName
    Map<String, PortList> portLists = new LinkedHashMap<>()

    ServerPortList(String serverName) {
        this.serverName = serverName
    }

    PortList setPortList(PortList portList) {
        portLists.put(portList.ip, portList)
    }
}
