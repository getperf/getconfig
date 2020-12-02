package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j
import java.sql.SQLException
import java.sql.SQLSyntaxErrorException
import com.xlson.groovycsv.CsvParser
import oracle.jdbc.*
import oracle.jdbc.pool.*

import com.getconfig.Model.Metric
import com.getconfig.Model.PlatformMetric
import com.getconfig.AgentWrapper.*
import com.getconfig.Model.Server

@Slf4j
@InheritConstructors
class Oracle implements DirectExecutorWrapper {
    Server server
    PlatformMetric platformMetric
    String currentLogDir
    int timeout = 0
    int level = 0

    @Override
    String getLabel() {
        return "oracleconf"
    }

    @Override
    void setEnvironment(DirectExecutor executor) {
        this.currentLogDir = executor.currentLogDir
        this.timeout = executor.timeout
        this.level = executor.level
        this.server = executor.server
        this.platformMetric = executor.platformMetric
    }

    @Override
    int run() {
        log.info("run Oracle direct executor ${this.server.serverName}")
        this.platformMetric?.getAll().each { Metric metric ->
            if (metric.level <= this.level) {
                return
            }
            println "ID:${metric.id},LV:${metric.level},${this.level}"
        }
        return 0
    }

    int dryRun() {
        log.info("run Oracle direct executor ${this.server.serverName}")
        this.platformMetric?.getAll().each { Metric metric ->
            if (metric.level <= this.level) {
                return
            }
            println "ID:${metric.id},LV:${metric.level},${this.level}"
        }
        return 0
    }
}
