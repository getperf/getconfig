package com.getconfig

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.getconfig.Command.GetconfigCommand
import com.getconfig.Document.SpecReader
import com.getconfig.Model.ResultGroup
import com.getconfig.Model.TestScenario
import com.getconfig.Model.Server
import com.getconfig.Model.ServerGroup
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory

@Slf4j
@ToString
//@CompileStatic
@CompileDynamic
class TestRunner implements Controller {
    boolean silent = false
    boolean dryRun = false
    String checkSheetPath
    String evidenceSheetPath
    List<Server> testServers
    Map<String, ServerGroup> testServerGroups
    Map<String, ResultGroup> testResultGroups

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
        log.info "run collector"
        Collector collector = new Collector(this.testServers)
        ConfigEnv.instance.accept(collector)
        collector.run()
        log.info "finish collector"
    }

    void runLogParser() {
        log.info "run parser"
        LogParser logParser = new LogParser(this.testServers)
        ConfigEnv.instance.accept(logParser)
        logParser.run()
        this.testResultGroups = logParser.testResultGroups
        log.info "finish parser"
    }

    void runReporter() {
        TestScenario testScenario = new TestScenario(
                testServers: this.testServers,
                testResultGroups: this.testResultGroups,
        )
        // メトリック定義の読込み
        // 検査結果の収集、サーバ、メトリック毎
        log.info "run report"
    }

    int run() {
        long start = System.currentTimeMillis()
        log.info new GetconfigCommand().getVersion() as String
        this.setLogLevel()
        this.readExcel()
        this.runCollector()
        this.runLogParser()

        this.runReporter()
        long elapse = System.currentTimeMillis() - start
        log.info "Finish, Total Elapse : ${elapse} ms"
        return 0
    }
}
