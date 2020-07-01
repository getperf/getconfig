package com.getconfig

import com.getconfig.Model.TestServer
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Collector {
    String gconfExe
    String tlsConfigDir
    String currentLogDir
    String gconfConfigDir
    protected List<TestServer> testServers = new ArrayList<TestServer>()
    // protected List<String> gconfCommands = new ArrayList<String>()
    // GconfInitializer gconfInit = GconfInitializer.instance.init()

    // final String excelFile = "サーバチェックシート.xlsx"
    // final String configFile = "config/config.groovy"
    // ConfigEnv env

    // Collector(String excelFile, configFile) {
    //     this.excelFile = excelFile
    //     this.configFile = configFile
    // }


    // void init() {
    //     if (!this.env) {
    //         this.env = ConfigEnv.instance
    //         this.env.readConfig(configFile)
    //     }
    //     // this.gconfConfigDir = this.env.getPlatformConfigDir()
    //     if (!this.gconfInit) {
    //         this.gconfInit = GconfInitializer.instance.init()
    //     }
    //     def specReader = new SpecReader(inExcel : excelFile)
    //     specReader.parse()
    //     specReader.mergeConfig()
    //     this.testServers = specReader.testServers()
    // }

    // String getGconfToml(TestServer server) {
    //     def converter = gconfInit.getConverter(server.domain)
    //     GconfConfig gconf = converter.convert(server)
    //     TomlWriter tomlWriter = new TomlWriter()
    //     return tomlWriter.write(gconf)
    // }

    // List<String> getGconfCommandArgs(TestServer server) {
    //     def args = new ArrayList<String>()
    //     args << "-t"
    // }

    void makeConfig() {
        testServers.each { server ->
            try {
                // def gconfCommand = new GconfCommandSet(this)
                // def toml = gconfCommand.toml(server)
                // gconfCommand.tomlPath()
                // gconfCommand.args()
                // def toml = this.getGconfToml(server)
                // println toml
            } catch (IllegalArgumentException e) {
                log.info "create ${server.serverName} config error, skip\n $e"
            }
            // TomlWriter tomlWriter = new TomlWriter();
            // println tomlWriter.write(server)
            // println server
        }
    }
}
