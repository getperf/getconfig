package com.getconfig

import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.exec.*
import java.nio.file.Path

@Slf4j
@CompileStatic
@TypeChecked
class CommandExec {
    int timeout = 0

    CommandExec(int timeout) {
        this.timeout = timeout
    }

    int run(String command, String[] args) {
        def outputStream = new ByteArrayOutputStream()
        def commandLine = new CommandLine(command)
        commandLine.addArguments(args)
        def executor = new DefaultExecutor()
        if (this.timeout > 0) {
            def watchdog = new ExecuteWatchdog(this.timeout)
            executor.setWatchdog(watchdog)
        }
        int rc = 1
        try {
            def streamHandler = new PumpStreamHandler(outputStream)
            executor.setStreamHandler(streamHandler)
            executor.setExitValue(0)    // 正常終了の場合に返される値
            rc = executor.execute(commandLine)
        } catch (ExecuteException e) {
            log.warn("execute error $command $args : $e")
        } catch (IOException e) {
            log.warn("file access error $command : $e")
        }
        log.debug(outputStream.toString())
        return rc
    }
}
