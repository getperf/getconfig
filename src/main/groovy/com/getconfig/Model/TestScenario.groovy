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
    List<String> servers
    Multimap<String, String> serverPlatformKeys
    Multimap<String, String> platformServerKeys
    Multimap<String, String> platformMetricKeys
    Map<String, String> metricIndex
    Multimap<String, String> portListKeys
    Multimap<String, String> resultSheetServerKeys
    Multimap<String, String> serverGroupTags

    // ファクト定義
    Table<String, String, Result> results
    Table<String, String, ResultLine> devices
    Table<String, String, ResultTag> resultTags
    Map<String, Metric> metrics
    Map<String, PortList> portLists

    // レポート定義
    ReportResult reportResult
    ReportSummary reportSummary
    Map<String, ResultSheet> resultSheets

    TestScenario() {
        this.servers = new ArrayList<>()
        this.serverPlatformKeys = LinkedHashMultimap.create()
        this.platformServerKeys = LinkedHashMultimap.create()
        this.platformMetricKeys = HashMultimap.create()
        this.portListKeys = HashMultimap.create()
        this.metricIndex = new LinkedHashMap<>()
        this.resultSheetServerKeys = LinkedHashMultimap.create()
        this.serverGroupTags = HashMultimap.create()

        this.results = HashBasedTable.create()
        this.resultTags = HashBasedTable.create()
        this.devices = HashBasedTable.create()
        this.metrics = new LinkedHashMap<>()
        this.portLists = new LinkedHashMap<>()
        this.resultSheets = new LinkedHashMap<>()
    }

    TestScenario setPortList(String serverName, String ip, PortList portList) {
        String serverIp = "${serverName}.${ip}"
        this.portListKeys.put(serverName, ip)
        this.portLists.put(serverIp, portList)

        return this
    }

    TestScenario setResult(String serverName, String metricId, Result result) {
        String platform = MetricId.platform(metricId)
        this.serverPlatformKeys.put(serverName, platform)
        this.platformServerKeys.put(platform, serverName)
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

        this.platformMetricKeys.put(platform, metricKey)
        this.metricIndex.put(metricId, metricKey)
        this.metrics.put(metricKey, metric)

        return this
    }

    TestScenario setChildMetric(int order, AddedMetric addedMetric) {
        String platform = addedMetric.platform
        String metricId = addedMetric.metricId()
        String parentMetricId = addedMetric.parentMetricId()

        String parentMetricKey = this.metricIndex.get(parentMetricId)
        if (!parentMetricKey) {
            log.error "'${parentMetricId}' parent metric not found for '${metricId}'"
            return
        }
        String metricKey = MetricId.orderChildKey(parentMetricKey, order)

        Metric baseMetric = this.metrics.get(parentMetricKey)
        Metric metric = Metric.make(addedMetric, baseMetric)

        this.platformMetricKeys.put(platform, metricKey)
        this.metricIndex.put(metricId, metricKey)
        this.metrics.put(metricKey, metric)

        return this
    }

    Map<String, String> getDomains() {
        Map<String, String> domains = new LinkedHashMap<>()
        this.resultSheetServerKeys.entries().each {
            domains.put(it.getValue(), it.getKey());
        }
        return domains
    }

    String getMetricKey(String metricId) {
        return this.metricIndex.get(metricId)
    }

    ResultSheet getResultSheet(String sheetName) {
        return this.resultSheets.get(sheetName)
    }

    List<String> getServers(String sheetName) {
        return this.resultSheetServerKeys.asMap().get(sheetName) as List<String>
    }

    String getResultSheetName(String serverName) {
        String resultSheetName = 'N/A'
        this.resultSheetServerKeys.asMap().each {
            String sheetName, Collection<String> serverNames ->
            if (serverName in serverNames) {
                resultSheetName = sheetName
                return
            }
        }
        return resultSheetName
    }

    String getTracker(String serverName) {
        String resultSheetName = this.getResultSheetName(serverName)
        if (!resultSheetName) {
            return
        }
        ResultSheet resultSheet = this.getResultSheet(resultSheetName)
        if (!resultSheet) {
            return
        }
        return resultSheet.tracker
    }

    boolean checkTagServer(String server) {
        return serverGroupTags.containsKey(server)
    }

    String getServerGroupTag(String server) {
        String compareServer
        this.serverGroupTags.entries().each {
            if (server == it.getValue()) {
                compareServer = it.getKey()
                return
            }
        }
        return compareServer
    }

    PortList getPortList(String server, String ip) {
        String portListKey = "${server}.${ip}"
        return this.portLists?.get(portListKey)
    }
}
