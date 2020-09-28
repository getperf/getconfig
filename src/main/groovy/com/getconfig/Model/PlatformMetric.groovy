package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class PlatformMetric {
    String platform
    List<Metric> metrics = new ArrayList<>()
    Map<String, Metric> dictMetrics = new LinkedHashMap<>()

    List<Metric> getAll() {
        return metrics
    }

    PlatformMetric validate() {
        this.metrics.each { Metric metric ->
            metric.platform = this.platform
            dictMetrics[metric.name] = metric
        }
        return this
    }

    Metric get(String metric) {
        return dictMetrics.get(metric)
    }
}
