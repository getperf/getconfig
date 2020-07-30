package com.getconfig.AgentLogParser.Platform2

import com.getconfig.AgentLogParser.Parser
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic

@Parser("uname")
void uname() {
    println "uname"
}
