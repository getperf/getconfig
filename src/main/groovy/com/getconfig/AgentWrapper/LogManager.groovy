package com.getconfig.AgentWrapper

import com.getconfig.Collector
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import java.nio.file.Paths

@TypeChecked
@CompileStatic
@Slf4j
class LogManager {
    static void restoreProjectLogs(Collector c, String currentLogDir) {
        if (currentLogDir.indexOf(c.currentLogDir) != 0) {
            throw new IllegalArgumentException("invarid path : ${currentLogDir}")
        }
        String currentLogBase = currentLogDir.substring(c.currentLogDir.size())
        String sourceLogPath = Paths.get(c.projectLogDir, currentLogBase)
        log.info("restore log ${currentLogBase}")
        try {
            def sourceDir = new File(sourceLogPath)
            def targetDir = new File(currentLogDir)
            targetDir.mkdirs()
            FileUtils.copyDirectory(sourceDir, targetDir)
        } catch (IOException e) {
            log.error("copy inventory dir : ${e}")
        }
    }
}
