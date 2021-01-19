package com.getconfig.Document

import com.getconfig.TestData
import com.getconfig.Model.*
import com.google.common.collect.Multimap
import com.google.common.collect.Table
import spock.lang.Specification

class TagGeneratorTest extends Specification {
    TestScenario testScenario

    def setup() {
        def testResultGroups = TestData.prepareResultGroupFromJson(
                'src/test/resources/node_compare_test'
        )
        testResultGroups.each { String serverName, ResultGroup resultGroup ->
            resultGroup.compareServer = null
        }
        TestScenarioManager manager = new TestScenarioManager(
                'lib/dictionary',
                testResultGroups)
        manager.run()
        this.testScenario = manager.getTestScenario()
    }

    def "GetSurrogateKeys"() {
        when:
        def tagGenerator = new TagGenerator(this.testScenario)
        tagGenerator.getSurrogateKey("abc", "123")

        then:
        tagGenerator.getSurrogateKey("abc", "123") == 1
    }

    def "KMeans法デモ"() {
        when:
        // Generate a random data set.
        // Note: ELKI has a nice data generator class, use that instead.
        double[][] data = new double[1000][2];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = Math.random();
            }
        }
        def result = KMeansCluster.run(data, 3)

        then:
        result.keySet() as List == [0, 1, 2]
        result.get(0).size() > 0
    }

    def "タグ生成"() {
        when:
        def tagGenerator = new TagGenerator(this.testScenario)
        def sheetServerKeys = testScenario.resultSheetServerKeys.asMap()
        sheetServerKeys.each { String sheetName, Collection<String> refServers ->
            List<String> servers = refServers as List<String>
            double[][] data = tagGenerator.makeDummyVariables(servers)
            Multimap<Integer, Integer> cluster = KMeansCluster.run(
                    data, tagGenerator.clusterSize)
            tagGenerator.setGroupTag(cluster, servers)
        }

        then:
        println this.testScenario.serverGroupTags
        this.testScenario.serverGroupTags.size() > 0
    }

    def "タグ生成run"() {
        when:
        def tagGenerator = new TagGenerator(this.testScenario, 2)
        tagGenerator.run()

        then:
        println this.testScenario.serverGroupTags
        this.testScenario.serverGroupTags.size() > 0
    }
}
