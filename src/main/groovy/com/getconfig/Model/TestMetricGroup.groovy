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
    Map<String, TestMetric> dictMetrics = new LinkedHashMap<>()

    List<TestMetric> getAll() {
        return metrics
    }

    TestMetricGroup validate() {
        this.metrics.each {TestMetric metric ->
            metric.platform = this.platform
            dictMetrics[metric.name] = metric
        }
        return this
    }

    TestMetric get(String metric) {
        return dictMetrics.get(metric)
    }
}
