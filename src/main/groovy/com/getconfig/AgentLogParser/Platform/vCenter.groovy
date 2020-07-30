package com.getconfig.AgentLogParser.Platform

import com.getconfig.AgentLogParser.AgentLogParser

class vCenter implements AgentLogParser {
    @Override
    int parse(InputStream input) {
        return 0
    }

    int datastoreJson(Reader reader) {
        println "PARSE datastore.json"
        reader.eachLine {
            println it
        }
        return 0
    }

}
