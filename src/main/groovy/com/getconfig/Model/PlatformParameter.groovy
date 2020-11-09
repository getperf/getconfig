package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class PlatformParameter {
    List<Object> values = new ArrayList<>()

    void add(Object value) {
        values.add(value)
    }
}
