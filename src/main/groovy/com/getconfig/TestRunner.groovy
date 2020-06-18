package com.getconfig

import groovy.transform.ToString
import com.moandjiezana.toml.TomlWriter

@ToString
class Server {
  String Server
  String Url
  String User
  String Password
}

@ToString
class Linux {
    String Server
    boolean local_exec
    Server[] Servers
}

class TestRunner {
    void run() {
        def servers = [
             new Server(Server:"ostrich", Url:"192.168.0.5", User:"psadmin", Password:"psadmin"),
             new Server(Server:"centos7", Url:"192.168.0.20", User:"psadmin", Password:"psadmin"),
        ]
        def linux = new Linux(Server:"cent80", local_exec:true, Servers:servers)

        TomlWriter tomlWriter = new TomlWriter();
        println tomlWriter.write(linux)
    }
}
