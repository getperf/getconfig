package com.getconfig.Document


import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.*
import com.getconfig.Utils.TomlUtils
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
class TestScenarioManager implements Controller {
    String metricLib
    TestScenario testScenario
    Map<String,ResultGroup> resultGroups
    Map<Integer,String> serverOrders = new LinkedHashMap<>()

    TestScenarioManager(String metricLib = null, 
                        Map<String,ResultGroup> resultGroups = null) {
        this.testScenario = new TestScenario()
        this.metricLib = metricLib
        this.resultGroups = resultGroups
    }

    void setEnvironment(ConfigEnv env) {
        this.metricLib = env.getMetricLib()
    }

    void setPortLists(String serverName, ServerPortList serverPortList) {
        serverPortList.portLists.each { 
            String ip, PortList portList ->
           this.testScenario.setPortList(serverName, ip, portList)
        }
    }

    void setResultGroup(String serverName, ResultGroup resultGroup) {
        this.serverOrders.put(resultGroup.order, serverName)
        if (resultGroup.compareServer) {
            this.testScenario.serverGroupTags.put(resultGroup.compareServer,
                serverName)
        }
        resultGroup.testResults.each { String metricId, Result result ->
            this.testScenario.setResult(serverName, metricId, result)
        }
        setPortLists(serverName, resultGroup.serverPortList)
    }

    TestScenario setResultGroups(Map<String,ResultGroup> resultGroups) {
        resultGroups.each { String serverName, ResultGroup resultGroup ->
            println "server:$serverName, compare: ${resultGroup.compareServer}"
            setResultGroup(serverName, resultGroup)
        }
        return this.testScenario
    }

    void setAddedMetrics(Map<String,ResultGroup> resultGroups) {
        resultGroups.each { String serverName, ResultGroup resultGroup ->
            int order = 1
            String oldParentMetricId
            resultGroup.addedTestMetrics.each { String metricId, AddedMetric addedMetric ->
                String parentMetricId = addedMetric.parentMetricId()
                if (!this.testScenario.setChildMetric(order, addedMetric)) {
                    return
                }
                order ++
                if (oldParentMetricId && oldParentMetricId != parentMetricId) {
                    order = 1
                }
                oldParentMetricId = parentMetricId
            }
        }
    }

    TestScenario setMetrics(String platform, 
                            PlatformMetric platformMetric) {
        int order = 1
        platformMetric.metrics.each { metric ->
            this.testScenario.setBaseMetric(platform, order, metric)
            order ++
        }
        return this.testScenario
    }

    TestScenario readMetrics() {
        if (!metricLib) {
            throw new IllegalArgumentException(
                "metricLib is not defined")
        }
        testScenario.platformServerKeys.keySet().each { String platform ->
            String tomlPath = Paths.get(metricLib, "${platform}.toml")
            PlatformMetric platformMetric = TomlUtils.read(tomlPath, PlatformMetric.class) as PlatformMetric
            setMetrics(platform, platformMetric)
        }
        return testScenario
    }

    TestScenario readReportConfig() {
        String resultConfig = Paths.get(metricLib,
                                    ExcelConstants.REPORT_RESULT_CONFIG_TOML)
        ReportResult reportResult = TomlUtils.read(resultConfig, ReportResult) as ReportResult
        reportResult.makePlatformIndex()
        testScenario.reportResult = reportResult

        String summaryConfig = Paths.get(metricLib,
                                     ExcelConstants.REPORT_SUMMARY_CONFIG_TOML)
        testScenario.reportSummary = TomlUtils.read(summaryConfig, ReportSummary) as ReportSummary

        return testScenario
    }

    TestScenario setServerToReport() {
        testScenario.with {
            this.serverOrders.sort().each {
                if (it.key > 0) {
                    servers.add(it.value)
                }
            }
            Map serverPlatformKeys = serverPlatformKeys.asMap()
            serverPlatformKeys.each { String server, Collection platforms ->
                ResultSheet resultSheet
                resultSheet = reportResult.findSheet(platforms as List)
                if (resultSheet) {
                    String sheetName = resultSheet.name
                    resultSheets.put(sheetName, resultSheet)
                    resultSheetServerKeys.put(sheetName, server)
                }
            }
        }
        return testScenario
    }

    int run() {
        if (!this.resultGroups || !this.metricLib) {
            log.error("result group or metric library path is not set")
            return 1
        }

        setResultGroups(this.resultGroups)
        readReportConfig()
        readMetrics()
        setAddedMetrics(this.resultGroups)
        setServerToReport()

        return 0
    }

}
