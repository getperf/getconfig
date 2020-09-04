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

    TestMetricGroup testMetrics
    List<TestServer> testServers
    Map <String, TestServerGroup> testServerGroups
    Map <String,TestResultGroup> testResultGroups
    PortListGroup portLists
}
