package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class TestScenario {
    String serverName
    String platform
    Map<String, TestResult> testResults
}
