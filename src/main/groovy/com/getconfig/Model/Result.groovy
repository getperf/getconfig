package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class Result {
    enum ResultStatus {
        OK, NG, WARNING, MATCH, UNMATCH, UNKNOWN
    }

    enum ColumnType {
        RESULT, TAGGING, UNKNOWN
    }

    String platform
    String metricName
    String parentMetric
    String serverName
    Object value
    String error
    String compareServer
    boolean excludeCompare
    ResultStatus status
    ResultStatus verify
    ResultStatus comparison
    ResultLine devices

    Result(String platform, String metricName, String serverName) {
        this.platform = platform
        this.metricName = metricName
        this.serverName = serverName
    }

    void compareValue(Result result) {
        this.comparison = ResultStatus.UNMATCH
        if (result) {
            if (this.value == result.value) {
                this.comparison = ResultStatus.MATCH
            }
        }
    }

    boolean isMatch() {
        return (this.comparison == ResultStatus.MATCH)
    }
}
