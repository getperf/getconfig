package com.getconfig.Model

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ResultGroup {
//    @ToString(includePackage = false)
//    class ResultKey {
//        final String platform
//        final String metricName
//
//        ResultKey(String platform, String metricName) {
//            this.platform = platform
//            this.metricName = metricName
//        }
//
//        @Override
//        boolean equals(Object obj) {
//            if (obj instanceof ResultKey) {
//                ResultKey condition = (ResultKey) obj
//                return this.platform == condition.platform &&
//                        this.metricName == condition.metricName
//            } else {
//                return false
//            }
//        }
//
//        @Override
//        int hashCode() {
//            return Objects.hash(platform, metricName)
//        }
//    }

    int order
    String serverName
    String compareServer
    Map<String, Result> testResults = new LinkedHashMap<>()
    ServerPortList serverPortList
    Map<String, AddedMetric> addedTestMetrics = new LinkedHashMap<>()

//    Map<String, PortList> portLists = new LinkedHashMap<>()

    ResultGroup(Server testServer) {
        this.order = testServer.order
        this.serverName = testServer.serverName
        this.compareServer = testServer.compareServer
        this.serverPortList = new ServerPortList(this.serverName)
    }

    ResultGroup(String serverName, String compareServer = null, int order = 0) {
        this.order = order
        this.serverName = serverName
        this.compareServer = compareServer
        this.serverPortList = new ServerPortList(this.serverName)
    }

    // String metricFullId(String platform, String metric) {
    //     return "${platform}.${metric}"
    // }

    // String MetricId.platform(String id) {
    //     int pos = id.indexOf(".")
    //     return (pos == -1) ? id : id.substring(0, pos)
    // }

    // String MetricId.metric(String id) {
    //     int pos = id.indexOf(".")
    //     return (pos == -1) ? id : id.substring(pos+1, id.length())
    // }

    void put(String platform, String metric, Result testResult) {
        this.testResults.put(MetricId.make(platform, metric), testResult)
    }

    List<String> getPlatforms() {
        Map<String, Boolean> platforms = new LinkedHashMap<>()
        this.testResults.each {resultKey, result ->
            platforms.put(MetricId.platform(resultKey), true)
        }
        return platforms.keySet() as List<String>
    }

    Result get(String platform, String metric) {
        return this.testResults.get(MetricId.make(platform, metric))
    }

    Result setValue(String platform, String metric, Object value, String parentMetric = null) {
        Result testResult = this.get(platform, metric)
        if (!testResult) {
            testResult = new Result(platform, metric, this.serverName)
        }
        testResult.parentMetric = parentMetric
        // GString の JSON のシリアライズでフォーマットエラーが発生するため、String型に変換
        if (value instanceof GString) {
            testResult.value = value as String
        } else {
            testResult.value = value
        }
        this.put(platform, metric, testResult)
        return testResult
    }

    AddedMetric getMetric(String platform, String metric) {
        return this.addedTestMetrics.get(MetricId.make(platform, metric))
    }

    void setMetric(String platform, String metric, AddedMetric newMetric) {
        String key = MetricId.make(platform, metric)
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
        this.serverPortList.setPortList(portList)
    }


}
