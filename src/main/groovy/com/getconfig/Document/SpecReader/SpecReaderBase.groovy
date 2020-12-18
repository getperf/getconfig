package com.getconfig.Document.SpecReader

import groovy.transform.CompileStatic

@CompileStatic
interface SpecReaderBase {
    ServerSpec read(String inFile)
}
