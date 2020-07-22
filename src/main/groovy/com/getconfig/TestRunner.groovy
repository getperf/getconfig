package com.getconfig

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.getconfig.Command.GetconfigCommand
import com.getconfig.Document.SpecReader
import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory

@Slf4j
@ToString
@CompileStatic
class TestRunner implements Controller {
    boolean silent = false
    boolean dryRun = false
    String checkSheetPath
    String evidenceSheetPath
    List<TestServer> testServers

    void setEnvironment(ConfigEnv env) {
        this.silent = env.getSilent()
        this.dryRun = env.getDryRun()
        this.checkSheetPath = env.getCheckSheetPath()
        this.evidenceSheetPath = env.getEvidenceSheetPath()
    }

    void setLogLevel() {
        def logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger)logger
        if (this.silent)
            log.setLevel(Level.OFF)
    }

    void readExcel() {
        def specReader = new SpecReader(inExcel : this.checkSheetPath)
        specReader.parse()
        specReader.mergeConfig()
        this.testServers = specReader.testServers()
        def serverCount = testServers.size()
        if (serverCount == 0)
            throw new IllegalArgumentException("no test servers in ${this.checkSheetPath}")
        log.info "read excel, found ${serverCount} servers"
    }

    void runCollector() {
        if (this.dryRun) {
            log.info "skip inventory collector, because dryRun is true"
            return
        }
        log.info "run collector"
        Collector collector = new Collector(this.testServers)
        ConfigEnv.instance.accept(collector)
        collector.run()
        log.info "finish collector"
    }

    void report() {
        log.info "run report"
    }

    int run() {
        long start = System.currentTimeMillis()
        log.info new GetconfigCommand().getVersion() as String
        this.setLogLevel()
        this.readExcel()
        this.runCollector()
        this.report()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish, Total Elapse : ${elapse} ms"
        return 0
    }
}
