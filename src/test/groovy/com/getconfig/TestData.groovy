package com.getconfig

import com.getconfig.Model.*
import com.getconfig.Document.*

class TestData {
    static String checkSheet = './src/test/resources/getconfig.xlsx'
    static String configFile = './src/test/resources/config/config.groovy'
    static String currentLogDir = './src/test/resources/inventory'
    static String parserLibPath = './lib/parser'

    static List<Server> readTestServers(String suffix = null) {
        def env = ConfigEnv.instance
        env.readConfig(configFile)
        String excelFile = checkSheet
        if (suffix) {
            excelFile = checkSheet.replace(".xlsx", "_${suffix}.xlsx")
        }
        def specReader = new SpecReader(inExcel : excelFile)
        specReader.parse()
        specReader.mergeConfig()
        return specReader.testServers()
    }

    static Map<String, ResultGroup> prepareResultGroup(String suffix = null) {
        List<Server> testServers = this.readTestServers(suffix)
        LogParser logParser = new LogParser(testServers)
        logParser.agentLogPath = this.currentLogDir
        logParser.parserLibPath = this.parserLibPath
        if (logParser.run() != 0) {
            throw new IllegalArgumentException("log parser failed")
        }
        return logParser.testResultGroups
    }

    static Map<String, ResultGroup> prepareResultGroupFromJson() {
        def resultGroupManager = new ResultGroupManager(
                nodeDir: 'src/test/resources/node'
        )
        return resultGroupManager.readAllResultGroups()
    }

}
