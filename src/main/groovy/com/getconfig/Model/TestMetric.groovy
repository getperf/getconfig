package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestMetric {
    String platform
    String metricId
    String metricName
    String category
    int commandLevel
    Boolean deviceFlag
    String comment
    String text

    Map <String, AddedTestMetric> addedTestMetrics

    TestMetric(String platform, String id, String name, String category,
               int level, boolean deviceFlag, String comment ) {
        this.platform = platform
        this.metricId = id
        this.metricName = name
        this.category = category
        this.commandLevel = level
        this.deviceFlag = deviceFlag
        this.comment = comment
    }

}
