package com.getconfig.Model

import com.google.common.collect.*
import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TestScenario {
    // 辞書定義
    Multimap<String, String> serverIndex
    Multimap<String, String> platformIndex
    Multimap<String, String> metricKeyIndex
    Multimap<String, String> portListIndex

    // ファクト定義
    Map<String, String> metricKeys
    Table<String, String, Result> results
    Table<String, String, ResultLine> devices
    Map<String, Metric> metrics
    Map<String, PortList> portLists

    // レポート定義
    ReportResult reportResult
    ReportSummary reportSummary
    Map<String, ResultSheet> resultSheets
    Multimap<String, String> resultSheetServers

    TestScenario() {
        this.serverIndex = HashMultimap.create()
        this.platformIndex = HashMultimap.create()
        this.metricKeyIndex = HashMultimap.create()
        this.portListIndex = HashMultimap.create()

        this.metricKeys = new LinkedHashMap<>()
        this.results = HashBasedTable.create()
        this.devices = HashBasedTable.create()
        this.metrics = new LinkedHashMap<>()
        this.portLists = new LinkedHashMap<>()
        this.resultSheets = new LinkedHashMap<>()
        this.resultSheetServers = HashMultimap.create()
    }

    TestScenario setPortList(String serverName, String ip, PortList portList) {
        String serverIp = "${serverName}.${ip}"
        this.portListIndex.put(serverName, ip)
        this.portLists.put(serverIp, portList)

        return this
    }

    TestScenario setResult(String serverName, String metricId, Result result) {
        String platform = MetricId.platform(metricId)
        this.serverIndex.put(serverName, platform)
        this.platformIndex.put(platform, serverName)
        // this.metricIndex.put(platform, metricId)
        this.results.put(serverName, metricId, result)
        if (result.devices) {
            this.devices.put(serverName, metricId, result.devices)
        }
        return this
    }

    TestScenario setBaseMetric(String platform, int order, Metric metric) {
        String metricId = MetricId.make(platform, metric.id)
        String metricKey = MetricId.orderParentKey(platform, order)

        this.metricKeyIndex.put(platform, metricKey)
        this.metricKeys.put(metricId, metricKey)
        this.metrics.put(metricKey, metric)

        return this
    }

    TestScenario setChildMetric(int order, AddedMetric addedMetric) {
        String platform = addedMetric.platform
        String metricId = addedMetric.metricId()
        String parentMetricId = addedMetric.parentMetricId()

        String parentMetricKey = this.metricKeys.get(parentMetricId)
        if (!parentMetricKey) {
            log.error "'${parentMetricId}' parent metric not found for '${metricId}'"
            return
        }
        String metricKey = MetricId.orderChildKey(parentMetricKey, order)

        Metric baseMetric = this.metrics.get(parentMetricKey)
        Metric metric = Metric.make(addedMetric, baseMetric)

        this.metricKeyIndex.put(platform, metricKey)
        this.metricKeys.put(metricId, metricKey)
        this.metrics.put(metricKey, metric)

        return this
    }

    Map<String, String> getDomains() {
        Map<String, String> domains = new LinkedHashMap<>()
        this.resultSheetServers.entries().each {
            domains.put(it.getValue(), it.getKey());
        }
        return domains
    }

    String getMetricKey(String metricId) {
        return this.metricKeys.get(metricId)
    }

    ResultSheet getResultSheet(String sheetName) {
        return this.resultSheets.get(sheetName)
    }

    List<String> getServers(String sheetName) {
        return this.resultSheetServers.asMap().get(sheetName) as List<String>
    }
}
