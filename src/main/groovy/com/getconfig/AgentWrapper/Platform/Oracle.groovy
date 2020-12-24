package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j
import groovy.sql.Sql
import java.sql.SQLException
import com.google.gson.Gson

import com.getconfig.Model.Metric
import com.getconfig.Model.PlatformMetric
import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

@TypeChecked
@CompileStatic
@Slf4j
@InheritConstructors
class Oracle implements DirectExecutorWrapper {
    Server server
    PlatformMetric platformMetric
    String logPath
    int timeout = 0
    int level = 0

    @Override
    String getLabel() {
        return "oracleconf"
    }

    @Override
    void setEnvironment(DirectExecutor executor) {
        this.logPath = executor.logPath
        this.timeout = executor.timeout
        this.level = executor.level
        this.server = executor.server
        this.platformMetric = executor.platformMetric
    }

    @Override
    int run() {
        log.info("run Oracle direct executor ${this.server.serverName}")
       Sql db
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver")
            def url = "jdbc:oracle:thin:@${server.ip}:${server.remoteAlias}"
            log.info "Connect: $url"
            db = Sql.newInstance(url, server.user, server.password)

        } catch (SQLException e) {
            def msg = "Oracle connection error : "
            throw new SQLException(msg + e)
        }
        this.platformMetric?.getAll().each { Metric metric ->
            if (metric.level > this.level) {
                return
            }
            if (!metric.text) {
                return
            }
            log.info "run ${metric.id}"
            try {
                def rows = db.rows(metric.text)
                String jsonText = new Gson().toJson(rows)
                new File("${this.logPath}/${metric.id}").text = jsonText
            } catch (SQLException e) {
                log.warn "run ${metric.id} : $e"
            }
        }
        try {
            db.close()

        } catch (SQLException e) {
            def msg = "Oracle connection error : "
            throw new SQLException(msg + e)
        }
        return 0
    }

    int dryRun() {
        log.info("run Oracle direct executor ${this.server.serverName}")
        log.info("logpath ${this.logPath}")
        this.platformMetric?.getAll().each { Metric metric ->
            if (metric.level > this.level) {
                return
            }
            // println "ID:${metric.id},LV:${metric.level},${this.level}"
        }
        return 0
    }
}
