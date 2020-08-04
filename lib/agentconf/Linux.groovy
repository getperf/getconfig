package gconf

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.GconfWrapper.*
import com.getconfig.Model.TestServer

@Slf4j
@CompileStatic
@InheritConstructors
class Linux implements GconfWrapper {
    String getLabel() {
        return "linuxconf"
    }

    String getConfigName() {
        return "linuxconf.toml"
    }

    GconfConfig convertAll(List<TestServer> servers) {
        // TODO: Create specification 
    }

    GconfConfig convert(TestServer server) {
        def config = new GconfConfig(
                server: 'localhost',
                local_exec: false,
                servers : [
                    new GconfServer(
                        server : server.serverName,
                        url : server.ip,
                        user : server.user,
                        password : server.password,
                        ssh_key : server.loginOption,
                        insecure : true,
                    ),
                ],
            )
        return config
    }

}
