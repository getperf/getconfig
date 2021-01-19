package com.getconfig.Document

import com.getconfig.Model.Result
import com.getconfig.Model.TestScenario
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Multimap
import com.google.common.collect.Table
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class TagGenerator {
    TestScenario testScenario
    int clusterSize

    Table<String, String, Integer> surrogateKeys

    TagGenerator(TestScenario testScenario, int clusterSize = 1) {
        this.testScenario = testScenario
        this.clusterSize = 1
        this.surrogateKeys = HashBasedTable.create()
    }

    Integer getSurrogateKey(String index, String value) {
        Integer surrogateKey = surrogateKeys.get(index, value)
        if (!surrogateKey) {
            Integer idMax = surrogateKeys.row(index).size()
            surrogateKey = new Integer(idMax + 1)
            surrogateKeys.put(index, value, surrogateKey)
        }
        return surrogateKey
    }

    double[][] makeDummyVariables(List<String> serverNames) {
        Table<String, String, Result> results = testScenario.results
        def rownum = serverNames.size()
        int colnum = results.columnKeySet().size()

        double[][] data = new double[rownum][colnum]
        int row = 0
        serverNames.each {String serverName ->
            Map<String, Result> serverResults = results.row(serverName)
            int col = 0
            serverResults.each {String metricId, Result result ->
                String value = results.get(serverName, metricId)?.value ?: 'N/A'
                Integer surrogateKey = this.getSurrogateKey(metricId, value)
                data[row][col] = (double)surrogateKey
                col ++
            }
            row ++
        }
        return data
    }

    void setGroupTag(Multimap<Integer, Integer> clusters, List<String> serverNames) {
        clusters.asMap().each {Integer primaryKey, Collection<Integer> offsets ->
            String compareServer = serverNames.get(primaryKey)
            offsets.each { Integer offset ->
                if (primaryKey != offset) {
                    String targetServer = serverNames.get(offset)
                    log.info "make tag ${compareServer} => ${targetServer}"
                    this.testScenario.serverGroupTags.put(compareServer, targetServer)
                }
            }
        }
    }

    void run() {
        def sheetServerKeys = testScenario.resultSheetServerKeys.asMap()
        sheetServerKeys.each { String sheetName, Collection<String> refServers ->
            List<String> servers = refServers as List<String>
            double[][] data = this.makeDummyVariables(servers)
            Multimap<Integer, Integer> cluster = KMeansCluster.run(
                    data, this.clusterSize)
            this.setGroupTag(cluster, servers)
        }
    }
}
