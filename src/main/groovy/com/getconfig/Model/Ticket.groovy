package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class Ticket {
    String server
    String tracker
    String status
    String domain
    Map<String,String> custom_fields = new LinkedHashMap<>()
    List<PortList> port_lists = new ArrayList<>()

    Ticket(String server, String tracker, String domain = null) {
        this.server = server
        this.tracker = tracker
        this.domain = domain
    }

    void addPortList(PortList portList) {
        this.port_lists.add(portList)
    }

    String getSubject() {
        String subject
        if (server) {
            subject = server
        } else {
            throw new IllegalArgumentException("get subject : ${this}")
        }
        return subject
    }
}
