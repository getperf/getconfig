package com.getconfig.TestItem

import com.getconfig.TestItem
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class TestResultRegister {
    static void results(TestItem t, String s) {
        println "SET ${s}"
    }

    static void results(TestItem t, Map<String, Object> values) {
        println "SET ${values}"
    }

    static void newMetric(TestItem t, String metric, String description, Object value, Map<String, Object> results) {

    }
}
