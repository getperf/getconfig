package com.getconfig.ProjectManager

import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Document.ResultGroupManager
import com.getconfig.Model.Result
import com.getconfig.Model.ResultGroup
import com.getconfig.Model.ResultLine
import com.getconfig.Model.Server
import groovy.sql.Sql
import groovy.transform.ToString

import java.sql.SQLException
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
@ToString
class InventoryLoaderDatabase implements Controller {

    String projectName
    String tenantName
    String url
    String username
    String password
    String driver
    String createSql
    Sql sql
    Map<String, Integer> dbCache = new LinkedHashMap<>()

    void setEnvironment(ConfigEnv env) {
        this.projectName = env.getProjectName()
        this.tenantName = env.getTenantName()
        this.url = env.getInventoryDBUrl()
        this.username = env.getInventoryDBUsername()
        this.password = env.getInventoryDBPassword()
        this.driver = env.getInventoryDBDriver()
        this.createSql = env.getInventoryDBCreateScript()
    }

    void initialize() throws IOException, SQLException {
        try {
            sql = Sql.newInstance(url, username, password, driver)
        } catch(SQLException e) {
            def msg = "Connection error in 'config/cmdb.groovy' : "
            throw new IllegalArgumentException(msg + e)
        }
        // Confirm existence of Version table. If not, execute db create script
        boolean cmdbExists = false
        try {
            List rows = sql.rows('select * from tenants')
            if (rows.size()) {
                cmdbExists = true
            }
        } catch (SQLException e) {
            log.warn "VERSION table not found in CMDB, Create tables"
        }
        if (!cmdbExists) {
            String createStatements = new File(this.createSql).text
            createStatements.split(/;\s*\n/).each {
                sql.execute it
            }
        }
    }

    int resistMaster(String tableName, Map<String,Object> columns) throws SQLException {
        String cache_key = tableName + columns.toString()
        if (dbCache.containsKey(cache_key)) {
            return dbCache.get(cache_key)
        }
        def conditions = []
        def values = []
        columns.each { String columnName, Object value ->
            conditions << "${columnName} = ?"
            values << value
        }
        int id = 0
        def query = "select id from ${tableName} where " +
                    conditions.join(' and ')
        def rows = sql.rows(query, values)
        if (rows != null && rows.size() == 1) {
            id = rows[0]['id'] as int
            dbCache.put(cache_key, id)
            return id
        }
        def table = sql.dataSet(tableName)
        try {
            table.add(columns as Map<String, Object>)
        } catch (SQLException e) {
            log.info "This table already have a data, Skip\n" +
                     "${tableName} : ${columns}"
        }
        rows = sql.rows(query, values)
        if (rows != null && rows.size() == 1) {
            id = rows[0]['id'] as int
            dbCache.put(cache_key, id)
        }
        return id
    }

    void resistMetric(int nodeId, int metricId, Object value) throws SQLException {
        sql.execute("delete from test_results where node_id = ? and metric_id = ?",
                [nodeId, metricId] as List<Object>)
        Map<String, Object> columns = new LinkedHashMap<>()
        columns.put('node_id', nodeId)
        columns.put('metric_id', metricId)
        if (value)
            columns.put('value', value as String)
        sql.dataSet('test_results').add(columns)
    }

    void resistDevice(int nodeId, int metricId, List<String> headers,
        List<List<Object>> csv) throws SQLException {
        sql.execute("delete from device_results where node_id = ? and metric_id = ?",
                     [nodeId, metricId] as List<Object>)
        def row = 1
        log.debug "Regist device: ${nodeId}, ${metricId}, ${headers}"
        csv.each { List<Object> line ->
            Map<String, Object> columns = new LinkedHashMap<>()
            columns.put('node_id', nodeId)
            columns.put('metric_id', metricId)
            columns.put('seq', row)
            def column = 0
            line.each { Object value ->
                def itemName = headers[column]
                columns.put('item_name', itemName)
                columns.put('value', value  as String)
                log.debug "Set device_results: $columns"
                sql.dataSet('device_results').add(columns)
                column ++
            }
            row ++
        }
    }

    void loadResultGroup(ResultGroup resultGroup) {
        String serverName = resultGroup.serverName
        int siteId = resistMaster("sites",
                [site_name: this.projectName] as Map<String, Object>)
        int tenantId = resistMaster("tenants",
                [tenant_name: this.tenantName] as Map<String, Object>)
        int nodeId = resistMaster("nodes",
                [node_name: serverName, tenant_id: tenantId] as Map<String, Object>)
        resistMaster("site_nodes",
                [site_id: siteId, node_id: nodeId] as Map<String, Object>)

        int loadRows = 0
        String deviceFlagSetSql = 'update metrics set device_flag = true where id = ?'
        resultGroup.testResults.each {String metricId0, Result result ->
            String platform = result.platform
            String metric = result.metricName
            String value = result.value
            int platformId = resistMaster("platforms",
                    [platform_name: platform] as Map<String, Object>)

            // デバイス付き結果の登録
            if (result.devices) {
                ResultLine devices = result.devices
                int metricId = resistMaster("metrics",
                        [metric_name: metric, platform_id: platformId] as Map<String, Object>)
                sql.execute(deviceFlagSetSql, metricId)
                resistMetric(nodeId, metricId, value)
                resistDevice(nodeId, metricId, devices.headers, devices.csv)
                loadRows++

            // 追加メトリックを含まないベースメトリック結果の登録
            } else if (!result.parentMetric) {
                int metricId = resistMaster("metrics",
                        [metric_name: metric, platform_id: platformId] as Map<String, Object>)
                resistMetric(nodeId, metricId, value)
                loadRows++
            }
        }
        log.info("load : ${serverName}, ${loadRows} rows")
    }

    void export(List<Server> servers, String projectNodeDir)
            throws IOException, SQLException {
        ResultGroupManager manager = new ResultGroupManager(nodeDir: projectNodeDir)
        servers*.serverName.unique().each { String serverName ->
            try {
                ResultGroup resultGroup = manager.readResultGroup(serverName)
                this.loadResultGroup(resultGroup)
            } catch (FileNotFoundException e) {
                log.error("not found node file : ${serverName}.json")
            }
        }
     }

    void showConfig() {
       println "mysql loader config"
       println "    url      : ${this.url}"
       println "    user     : ${this.username}"
       println "    password : ${this.password}"
       println "    driver   : ${this.driver}"
    }

    int run() {
        return 0
    }
}
