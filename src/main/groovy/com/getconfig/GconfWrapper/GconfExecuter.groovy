package com.getconfig.GconfWrapper

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import com.moandjiezana.toml.TomlWriter
import java.nio.file.Paths 

import com.getconfig.Collector
import com.getconfig.Model.*
import com.getconfig.Document.*

@ToString
@CompileStatic
class GconfExecuter {
    String platform
    TestServer server
    GconfWrapper wrapper

    String toml() {
        GconfConfig gconf = wrapper.convert(server)
        TomlWriter tomlWriter = new TomlWriter()
        return tomlWriter.write(gconf)
    }

    String label() {
        return wrapper.getLabel()
    }

    String tomlFile() {
        def label = this.label()
        def serverName = this.server.serverName
        return "${label}__${serverName}.toml"
    }

    List<String> args() {
        def args = new ArrayList<String>()
        args.addAll("-t", this.label())
        args.addAll("-c", this.tomlFile())
        args.addAll("run")
        args.addAll("-o", "/tmp")
        return args
    }
}

