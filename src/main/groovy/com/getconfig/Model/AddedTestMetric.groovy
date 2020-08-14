package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class AddedTestMetric {
    String platform
    String metricName
    String parentMetric
    String comment

    AddedTestMetric(String platform, String metricName, String parentMetric, String comment = null) {
        this.platform = platform
        this.metricName = metricName
        this.parentMetric = parentMetric
        this.comment = comment
    }
}
