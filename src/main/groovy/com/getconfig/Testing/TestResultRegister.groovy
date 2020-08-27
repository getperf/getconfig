package com.getconfig.Testing

import com.getconfig.Model.AddedTestMetric
import com.getconfig.Model.TestResultGroup
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class TestResultRegister {
    static void results(TestUtil t, String s) {
        t.testResultGroup.setValue(t.platform, t.metricFile, s)
        println "SET ${s}"
    }

    static void results(TestUtil t, Map<String, Object> values) {
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

    static void setMetric(TestUtil t, String metric, Object value) {
        t.testResultGroup.setValue(t.platform, metric, value)
        println "SET ${metric},${value}"
    }

    static void error(TestUtil t, String s) {
        t.testResultGroup.setError(t.platform, t.metricFile, s)
        println "ERR ${s}"
    }

    static void devices(TestUtil t, List headers, List csv) {
        t.testResultGroup.setDevices(t.platform, t.metricFile, headers, csv)
    }

    static void newMetric(TestUtil t, String metric, String description, Object value) {
        AddedTestMetric addedTestMetric = t.addedTestMetrics.get(metric) ?:
                new AddedTestMetric(t.platform, metric, t.metricFile, description)
        t.addedTestMetrics.put(metric, addedTestMetric)
        t.testResultGroup.setValue(t.platform, metric, value, t.metricFile)
        println "SET ${metric},${value}"
    }
}
