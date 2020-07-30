package com.getconfig.AgentLogParser.Platform

import com.getconfig.AgentLogParser.AgentLogParser
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class Linux implements AgentLogParser {
    @Override
    int parse(InputStream input) {
        return 0
    }

    int uname(Reader reader) {
        println "PARSE uname"
        reader.eachLine {
            println it
        }
        return 0
    }
}
