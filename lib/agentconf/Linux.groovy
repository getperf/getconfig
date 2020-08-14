package com.getconfig.AgentWrapper.Platform

import groovy.transform.*
import groovy.util.logging.Slf4j

import com.getconfig.AgentWrapper.*
import com.getconfig.Model.TestServer
import com.getconfig.Model.TestMetric

@Slf4j
@InheritConstructors
class Linux implements AgentConfigWrapper {
    class Server {
        String  server
        String  url
        String  user
        String  password
        String  ssh_key
        boolean insecure
    }

    class Command {
        String id
        int level
        String type
        String text
    }

    class LinuxConfig {
        String server
        boolean local_exec
        List<Server> servers = new ArrayList<>()
        List<Command> commands
    }

    @Override
    String getLabel() {
        return "linuxconf"
    }

    @Override
    String getConfigName() {
        return "linuxconf.toml"
    }

    @Override
    boolean getBatchEnable() {
        return false
    }

    @Override
    def makeAllServersConfig(List<TestServer> servers, List<TestMetric> testMetrics = null) {
        // TODO: Create specification
    }

    List<Command>makeCommands(List<TestMetric>testMetrics) {
        List<Command> commands = new ArrayList<>()
        testMetrics.each {TestMetric metric ->
            if (metric.commandLevel >= 0) {
//                Command command = new Command(metric.metricId, metric.commandLevel, metric.text)
                Command command = new Command(id: "aaa")
//                commands.add()
            }
        }
        return commands
    }

    @Override
    def makeServerConfig(TestServer server, List<TestMetric> testMetrics = null) {
        def config = new LinuxConfig(
                server: 'localhost',
                local_exec: false,
                servers: [
                        new Server(
                                server : server.serverName,
                                url : server.ip,
                                user : server.user,
                                password : server.password,
                                ssh_key : server.loginOption,
                                insecure : true,
                        ),
                ],
        )
        if (testMetrics) {
            config.commands = new ArrayList<>()
            testMetrics.each {TestMetric testMetric ->
                if (testMetric.text) {
                    config.commands.add(
                            new Command(
                                    id: testMetric.metricId,
                                    level: testMetric.commandLevel,
                                    text: testMetric.text,
                            )
                    )
                }
            }
//            config.commands = this.makeCommands(testMetrics)
        }
        return config
    }
}
