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

    Ticket(String server, String tracker, String domain = null) {
        this.server = server
        this.tracker = tracker
        this.domain = domain
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
