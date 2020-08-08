package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestMetricGroup {
    String platform
    List<TestMetric> metrics = new ArrayList<>()

    List<TestMetric> getAll() {
        return metrics
    }

    TestMetricGroup validate() {
        this.metrics.each {TestMetric metric ->
            metric.platform = this.platform
        }
        return this
    }
}
