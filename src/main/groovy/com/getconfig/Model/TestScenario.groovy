package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestScenario {
    String platform
    String description

    MetricGroup testMetrics
    List<Server> testServers
    Map <String, ServerGroup> testServerGroups
    Map <String,ResultGroup> testResultGroups
    PortListGroup portLists
}
