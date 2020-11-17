package com.getconfig


import com.getconfig.ProjectManager.InventoryLoaderDatabase
import com.getconfig.ProjectManager.InventoryLoaderLocal
import com.getconfig.Document.SpecReader
import com.getconfig.Model.Server
import com.getconfig.ProjectManager.TicketExporter
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString
@CompileStatic
class Exporter implements Controller {
    String checkSheetPath
    String projectNodeDir
    String mode
    List<Server> testServers
    InventoryLoaderLocal inventoryLoaderLocal = new InventoryLoaderLocal()
    InventoryLoaderDatabase inventoryLoaderDatabase = new InventoryLoaderDatabase()
    TicketExporter ticketExporter = new TicketExporter()

    Exporter(String mode = 'local') {
        this.mode = mode
    }

    void setEnvironment(ConfigEnv env) {
        env.accept(inventoryLoaderLocal)
        env.accept(inventoryLoaderDatabase)
        env.accept(ticketExporter)
        this.checkSheetPath = env.getCheckSheetPath()
        this.projectNodeDir = env.getProjectNodeDir()
        this.mode = env.getTargetType()
    }

    void readExcel() {
        def specReader = new SpecReader(inExcel : this.checkSheetPath)
        specReader.parse()
        this.testServers = specReader.testServers()
        def serverCount = testServers.size()
        if (serverCount == 0)
            throw new IllegalArgumentException("no test servers in ${this.checkSheetPath}")
        log.info "read excel, found ${serverCount} servers"
    }

    void runExporter() {
//        projectManager.update(testServers, mode)
        switch (mode) {
            case 'local' :
                inventoryLoaderLocal.run()
                break

            case 'db' :
                inventoryLoaderDatabase.initialize()
                inventoryLoaderDatabase.export(testServers, projectNodeDir)
                break

            case 'ticket' :
                ticketExporter.export(testServers, projectNodeDir)
                break

            default :
                throw new IllegalArgumentException("unkown mode : ${mode}")
        }

    }
    
    int run() {
        long start = System.currentTimeMillis()
        this.readExcel()
        this.runExporter()
        long elapse = System.currentTimeMillis() - start
        log.info "total elapse : ${elapse} ms"
        return 0
    }
}
