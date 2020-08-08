package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class AddedTestMetric extends TestMetric {
    AddedTestMetric(String platform, String id, String name, String category, MetricCommandLevel type, boolean deviceFlag, String comment) {
        super(platform, id, name, category, type, deviceFlag, comment)
    }
}
