package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestResultGroup {
    @ToString(includePackage = false)
    class ResultKey {
        private final String platform
        private final String metricName

        ResultKey(String platform, String metricName) {
            this.platform = platform
            this.metricName = metricName
        }
        @Override
        boolean equals(Object obj) {
            if (obj instanceof ResultKey) {
                ResultKey condition = (ResultKey) obj
                return this.platform == condition.platform &&
                        this.metricName == condition.metricName
            } else {
                return false
            }
        }

        @Override
        int hashCode() {
            return Objects.hash(platform, metricName)
        }
    }

    String serverName
    String compareServer
    Map<ResultKey, TestResult> testResults = new LinkedHashMap<>()
    Map<String, PortList> portLists = new LinkedHashMap<>()

    TestResultGroup(TestServer testServer) {
        this.serverName = testServer.serverName
        this.compareServer = testServer.compareServer
    }

    TestResultGroup(String serverName, String compareServer = null) {
        this.serverName = serverName
        this.compareServer = compareServer
    }

    void put(String platform, String metric, TestResult testResult) {
        this.testResults.put(new ResultKey(platform, metric), testResult)
    }

    TestResult get(String platform, String metric) {
        return this.testResults.get(new ResultKey(platform, metric))
    }

    TestResult setValue(String platform, String metric, Object value, String parentMetric = null) {
        TestResult testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new TestResult(platform, metric, this.serverName)
        }
        testResult.parentMetric = parentMetric
        testResult.value = value
        this.put(platform, metric, testResult)
        return testResult
    }

    TestResult setError(String platform, String metric, String error) {
        TestResult testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new TestResult(platform, metric, this.serverName)
        }
        testResult.error = error
        this.put(platform, metric, testResult)
        return testResult
    }

    TestResult setDevices(String platform, String metric, List headers, List csv) {
        TestResult testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new TestResult(platform, metric, this.serverName)
        }
        TestResultLine testResultLine = new TestResultLine(headers: headers, csv: csv)
        testResult.devices = testResultLine
        this.put(platform, metric, testResult)
        return testResult
    }

    PortList setPortList(PortList portList) {
        portLists.put(portList.ip, portList)
    }
}
