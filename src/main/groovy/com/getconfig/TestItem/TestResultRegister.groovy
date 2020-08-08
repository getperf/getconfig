package com.getconfig.TestItem

import com.getconfig.Model.TestResult
import com.getconfig.Model.TestResultGroup
import com.getconfig.TestItem
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class TestResultRegister {
    static void results(TestItem t, String s) {
        t.testResultGroup.setValue(t.platform, t.metricFile, s)
        println "SET ${s}"
    }

    static void results(TestItem t, Map<String, Object> values) {
        TestResultGroup testResultGroup = t.testResultGroup
        values.each { String metricName, Object value ->
            println "${t.metricFile}, ${metricName}"
            if (t.metricFile == metricName) {
                t.testResultGroup.setValue(t.platform, metricName, value)
            } else {
                t.testResultGroup.setValue(t.platform, metricName, value, t.metricFile)
            }
        }
        println "SET ${values}"
    }

    static void error(TestItem t, String s) {
        t.testResultGroup.setError(t.platform, t.metricFile, s)
        println "ERR ${s}"
    }

    static void devices(TestItem t, List headers, List csv) {
        t.testResultGroup.setDevices(t.platform, t.metricFile, headers, csv)
    }

    static void newMetric(TestItem t, String metric, String description, Object value, Map<String, Object> results) {

    }
}
