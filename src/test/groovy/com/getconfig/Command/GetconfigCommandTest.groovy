package com.getconfig.Command

import com.getconfig.ConfigEnv
import picocli.CommandLine
import spock.lang.Specification

class GetconfigCommandTest extends Specification {
    CommandLine.IFactory factory
    GetconfigCommand getconfigCommand = new GetconfigCommand()

    def "ヘルプ出力"() {
        when:
        String help = new CommandLine(getconfigCommand).getUsageMessage(CommandLine.Help.Ansi.OFF);
        println help

        then:
        help.size() > 0
    }

    def "リストサブコマンドヘルプ"() {
        when:
        CommandLine cmd = new CommandLine(getconfigCommand)
        StringWriter sw = new StringWriter()
        cmd.setOut(new PrintWriter(sw))
        int exitCode = cmd.execute("ls", "--all")
        println sw

        then:
        ConfigEnv.instance.getAllFlag() == true
    }

    def "Runサブコマンドヘルプ"() {
        when:
        CommandLine cmd = new CommandLine(getconfigCommand)
        StringWriter sw = new StringWriter()
        cmd.setOut(new PrintWriter(sw))
        int exitCode = cmd.execute("run", "-h")
//        println sw

        println "ConfigFile : ${ConfigEnv.instance.getConfigFile()}"
        println "CheckSheet : ${ConfigEnv.instance.getCheckSheetPath()}"

        then:
        ConfigEnv.instance.getDryRun() == false
        exitCode == 0
    }

    def "ドライラン"() {
        when:
        CommandLine cmd = new CommandLine(getconfigCommand)
        StringWriter sw = new StringWriter()
        cmd.setOut(new PrintWriter(sw))
        int exitCode = cmd.execute("run", "-d")
        println sw
        println "CheckSheet : ${ConfigEnv.instance.getCheckSheetPath()}"

        then:
        ConfigEnv.instance.getDryRun() == true
        exitCode == 0
    }
}
