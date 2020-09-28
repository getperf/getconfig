package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class Metric {
    String platform
    String id
    String name
    String category
    int level
    Boolean deviceFlag
    String comment
    String text

    Map <String, AddedMetric> addedTestMetrics

    Metric(String platform, String id, String name, String category,
           int level, boolean deviceFlag, String comment ) {
        this.platform = platform
        this.id = id
        this.name = name
        this.category = category
        this.level = level
        this.deviceFlag = deviceFlag
        this.comment = comment
    }

    static Metric make(AddedMetric addedMetric, Metric parent = null) {
        String platform = addedMetric.platform
        String id = addedMetric.metricName
        String name = addedMetric.comment ?: parent?.name
        String category = parent?.category
        String comment = parent?.comment

        return new Metric(platform, id, name, category, -1, false, comment)
    }
}
