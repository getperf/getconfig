package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class PortList {
    String ip
    String device
    boolean forManagement

    Map<String,String> customFields() {
        Map<String, String> fields = new LinkedHashMap<>()
        fields.put("description", device)
        fields.put("managed", (forManagement)?"1":"0")
        return fields
    }
}
