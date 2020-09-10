package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.apache.commons.math3.analysis.function.Add

import javax.sound.sampled.Port

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ResultGroup {
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
    Map<ResultKey, Result> testResults = new LinkedHashMap<>()
    PortListGroup portListGroup
    Map<ResultKey, AddedMetric> addedTestMetrics = new LinkedHashMap<>()

//    Map<String, PortList> portLists = new LinkedHashMap<>()

    ResultGroup(Server testServer) {
        this.serverName = testServer.serverName
        this.compareServer = testServer.compareServer
        this.portListGroup = new PortListGroup(this.serverName)
    }

    ResultGroup(String serverName, String compareServer = null) {
        this.serverName = serverName
        this.compareServer = compareServer
        this.portListGroup = new PortListGroup(this.serverName)
    }

    void put(String platform, String metric, Result testResult) {
        this.testResults.put(new ResultKey(platform, metric), testResult)
    }

    Result get(String platform, String metric) {
        return this.testResults.get(new ResultKey(platform, metric))
    }

    Result setValue(String platform, String metric, Object value, String parentMetric = null) {
        Result testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new Result(platform, metric, this.serverName)
        }
        testResult.parentMetric = parentMetric
        testResult.value = value
        this.put(platform, metric, testResult)
        return testResult
    }

    AddedMetric getMetric(String platform, String metric) {
        return this.addedTestMetrics.get(new ResultKey(platform, metric))
    }

    void setMetric(String platform, String metric, AddedMetric newMetric) {
        ResultKey key = new ResultKey(platform, metric)
        if (!this.addedTestMetrics.get(key)) {
            this.addedTestMetrics.put(key, newMetric)
        }
    }

    Result setError(String platform, String metric, String error) {
        Result testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new Result(platform, metric, this.serverName)
        }
        testResult.error = error
        this.put(platform, metric, testResult)
        return testResult
    }

    Result setDevices(String platform, String metric, List headers, List csv) {
        Result testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new Result(platform, metric, this.serverName)
        }
        ResultLine testResultLine = new ResultLine(headers: headers, csv: csv)
        testResult.devices = testResultLine
        this.put(platform, metric, testResult)
        return testResult
    }

    PortList setPortList(PortList portList) {
        this.portListGroup.setPortList(portList)
    }
}
