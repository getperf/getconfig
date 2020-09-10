package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestScenario {
    List<Server> testServers
    Map <String,ResultGroup> testResultGroups
    Map<String,MetricGroup> testMetricGroups
}
