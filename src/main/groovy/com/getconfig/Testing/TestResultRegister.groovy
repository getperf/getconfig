package com.getconfig.Testing

import com.getconfig.Model.AddedTestMetric
import com.getconfig.Model.TestResultGroup
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class TestResultRegister {
    static void results(TestUtil t, String s) {
        t.testResultGroup.setValue(t.platform, t.metricFile, s)
         log.debug "SET ${s}"
    }

    static void results(TestUtil t, Map<String, Object> values) {
        TestResultGroup testResultGroup = t.testResultGroup
        values.each { String metricName, Object value ->
            log.debug "${t.metricFile}, ${metricName}"
            if (t.metricFile == metricName) {
                t.testResultGroup.setValue(t.platform, metricName, value)
            } else {
                t.testResultGroup.setValue(t.platform, metricName, value, t.metricFile)
            }
        }
        log.debug  "SET ${values}"
    }

    static void setMetric(TestUtil t, String metric, Object value) {
        t.testResultGroup.setValue(t.platform, metric, value)
        log.debug  "SET ${metric},${value}"
    }

    static void error(TestUtil t, String s) {
        t.testResultGroup.setError(t.platform, t.metricFile, s)
        log.debug  "ERR ${s}"
    }

    static void devices(TestUtil t, List headers, List csv) {
        t.testResultGroup.setDevices(t.platform, t.metricFile, headers, csv)
    }

    static void newMetric(TestUtil t, String metric, String description, Object value) {
        AddedTestMetric addedTestMetric = t.addedTestMetrics.get(metric) ?:
                new AddedTestMetric(t.platform, metric, t.metricFile, description)
        t.addedTestMetrics.put(metric, addedTestMetric)
        t.testResultGroup.setValue(t.platform, metric, value, t.metricFile)
        log.debug  "SET ${metric},${value}"
    }
}
