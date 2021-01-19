package com.getconfig.ProjectManager

import com.getconfig.Model.Server
import com.getconfig.TestData
import groovy.json.JsonBuilder
import com.getconfig.ConfigEnv
import spock.lang.Specification

import java.sql.SQLException

class InventoryLoaderDatabaseTest extends Specification {
    ConfigEnv configEnv = ConfigEnv.instance
    InventoryLoaderDatabase loader = new InventoryLoaderDatabase()

    def setup() {
        configEnv.readConfig('config/config.groovy')
        configEnv.dbConfig = null
        configEnv.accept(loader)
        loader.initialize()
    }

    def "Initialize"() {
        when:
        loader.initialize()

        then:
        loader.sql != null
    }

    def "マスター登録"() {
        when:
        def id = loader.resistMaster("sites", [site_name: 'site01'])
        def site = loader.sql.rows("select * from sites")

        then:
        println new JsonBuilder( site ).toPrettyString()
        site[0]['id'] > 0
        site[0]['site_name'].size() > 0
    }

    def "マスター重複登録"() {
        when:
        def id1 = loader.resistMaster("tenants", [tenant_name: '_Default'])
        def id2 = loader.resistMaster("tenants", [tenant_name: '_Default'])
        def tenant = loader.sql.rows("select * from tenants")

        then:
        id1 == id2
        tenant.size() == 1
    }

    def "マスター登録複数列"() {
        when:
        def id = loader.resistMaster("nodes", [node_name: 'node01', tenant_id: 1])
        def node = loader.sql.rows("select * from nodes where node_name = 'node01'")

        then:
        id > 0
        node[0]['node_name'] == 'node01'
        node[0]['tenant_id'] == 1
    }

    def "複数サイトノード登録"() {
        when:
        def site_id1      = loader.resistMaster("sites", [site_name: 'site01'])
        def site_id2      = loader.resistMaster("sites", [site_name: 'site02'])
        def node_id1      = loader.resistMaster("nodes", [node_name: 'node01', tenant_id: 1])
        def site_node_id1 = loader.resistMaster("site_nodes", [node_id: node_id1, site_id: site_id1])

        def node_id2      = loader.resistMaster("nodes", [node_name: 'node01', tenant_id: 1])
        def site_node_id2 = loader.resistMaster("site_nodes", [node_id: node_id2, site_id: site_id2])

        then:
        site_node_id1 != site_node_id2
        def sql = "select * from site_nodes where node_id = ? order by site_id"
        def site_node = loader.sql.rows(sql, node_id1)
        site_node[0]['site_id'] == site_id1
        site_node[0]['node_id'] == node_id1
        site_node[1]['site_id'] == site_id2
        site_node[1]['node_id'] == node_id1
    }

    def "マスター登録列名なし"() {
        when:
        def id = loader.resistMaster("nodes", [HOGE: 'node01', tenant_id: 1])

        then:
        thrown(SQLException)
    }

    def "マスター登録キャッシュ"() {
        when:
        def id1 = loader.resistMaster("sites", [site_name: 'site01'])
        def id2 = loader.resistMaster("sites", [site_name: 'site01'])

        then:
        id1 == id2
    }

    def "メトリック登録"() {
        when:
        loader.resistMetric(1, 1, "value01")

        def sql = "select * from test_results where node_id = ? and metric_id = ?"
        def rows = loader.sql.rows(sql, [1, 1])

        then:
        println new JsonBuilder( rows ).toPrettyString()
        rows[0]["VALUE"] == "value01"
    }

    def "デバイス登録"() {
        when:
        def metric_id = loader.resistMaster("metrics",
                [metric_name: 'metric1',
                 platform_id: 1])
        def devices = [
                header: ['value', 'verify'],
                csv: [
                        ["value01", true],
                        ["value02", false],
                        ["value03"],
                ]
        ]
        loader.resistDevice(1, metric_id, devices.header, devices.csv)

        def rows = loader.sql.rows(
                "select * from device_results where node_id = ? and metric_id = ?",
                [1, metric_id])
        println rows

        def rows2 = loader.sql.rows(
            "select * from metrics where id = ?",
            metric_id)

        then:
        rows.size() == 5
    }

    def "ノード定義のエクスポート"() {
        when:
        List<Server> testServers = TestData.readTestServers()
        loader.export(testServers, 'src/test/resources/node/')
        def node = loader.sql.rows("select * from nodes")
        def result = loader.sql.rows("select * from test_results where node_id = 1")
        def metric = loader.sql.rows("select * from metrics")

        then:
        println new JsonBuilder( node ).toPrettyString()
        println new JsonBuilder( result ).toPrettyString()
        node[0]['node_name'].size() > 0
        node[1]['node_name'].size() > 0
        result.size() > 0
    }

}
