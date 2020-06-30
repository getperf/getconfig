package com.getconfig.GconfWrapper

import groovy.transform.*
import groovy.util.logging.Slf4j
import groovy.transform.ToString

import com.getconfig.Model.*
import com.getconfig.Document.*

@CompileStatic
interface GconfWrapper {
    GconfConfig convertAll(List<TestServer> servers)
    GconfConfig convert(TestServer server)
    String getConfigName()
    String getLabel()
}

