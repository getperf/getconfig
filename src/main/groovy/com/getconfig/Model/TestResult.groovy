package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class TestResult {
    enum ResultStatus {
        OK, NG, WARNING, MATCH, UNMATCH, UNKNOWN
    }

    enum ColumnType {
        RESULT, TAGGING, UNKNOWN
    }

    String platform
    String metricName
    String serverName
    Object value
    String error
    String compareServer
    boolean excludeCompare
    ResultStatus status
    ResultStatus verify
    ResultStatus comparison
    TestResultLine devices
}
