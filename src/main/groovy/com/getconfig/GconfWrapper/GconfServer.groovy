package com.getconfig.GconfWrapper

import groovy.transform.*

@ToString
@CompileStatic
class GconfServer {
    String  server
    String  url
    String  user
    String  password
    String  ssh_key
    boolean insecure
}
