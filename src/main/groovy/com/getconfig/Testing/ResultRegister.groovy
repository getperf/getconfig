package com.getconfig.Testing

import com.getconfig.Model.AddedMetric
import com.getconfig.Model.ResultGroup
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class ResultRegister {
    static void results(TestUtil t, String s) {
        t.testResultGroup.setValue(t.platform, t.metricFile, s)
         log.debug "SET ${s}"
    }

    static void results(TestUtil t, Map<String, Object> values) {
        ResultGroup testResultGroup = t.testResultGroup
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

    static void devices(TestUtil t, List headers, List csv, String metric = null) {
        String metricFile = metric ?: t.metricFile
        t.testResultGroup.setDevices(t.platform, metricFile, headers, csv)
    }

    static void newMetric(TestUtil t, String metric, String description, Object value) {
        if (!t.testResultGroup.getMetric(t.platform, metric)) {
            t.testResultGroup.setMetric(t.platform, metric,
                new AddedMetric(t.platform, metric, t.metricFile, description))
        }
        t.testResultGroup.setValue(t.platform, metric, value, t.metricFile)
        log.debug  "SET ${metric},${value}"
    }

    static void resetMetric(TestUtil t, String metricFile) {
        t.metricFile = metricFile
    }
}
