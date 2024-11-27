package com.getconfig.Command

import com.getconfig.ProjectManager.BackupManager
import com.getconfig.ProjectManager.ConfigFileEncoder
import com.getconfig.ProjectManager.ProjectConstants
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ExitCode
import picocli.CommandLine.IVersionProvider
import picocli.CommandLine.IExitCodeExceptionMapper
import java.util.concurrent.Callable
import com.getconfig.ConfigEnv
import com.getconfig.TestRunner
import com.getconfig.Exporter
import com.getconfig.ProjectManager.ProjectInitializer

@Slf4j
@CompileStatic
@TypeChecked
@Command(name = "getconfig", mixinStandardHelpOptions = true,
        versionProvider = GetconfigCommand.class,
        description = "Getconfig inventory collector",
        subcommands = [
                GetconfigCommand.InitCommand.class,
                GetconfigCommand.RunCommand.class,
                GetconfigCommand.BackupCommand.class,
                // GetconfigCommand.ListCommand.class,
                GetconfigCommand.UpdateCommand.class,
                // GetconfigCommand.RegistCommand.class,
                GetconfigCommand.EncodeCommand.class
        ])
public class GetconfigCommand implements Callable<Integer>, IVersionProvider, IExitCodeExceptionMapper {

    // picocli.AutoComplete で generate Completion Script をする時に引数なしのコンストラクタが必要になる
    // のでコンストラクタインジェクションは使用しないこと
    // https://picocli.info/autocomplete.html 参照
    // @Autowired
    // private BuildProperties buildProperties;

    @Override
    public Integer call() {
        println "run base"
        return ExitCode.OK;
    }

    @Override
    public int getExitCode(Throwable exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof NumberFormatException) {
            // 数値パラメータに数値以外の文字が指定された
            return 12;
        }

        return 11;
    }

    @Override
    public String[] getVersion() {
        // return new String[]{buildProperties.getVersion()};
        return new String[]{"Getconfig inventory collector v" +
                ProjectConstants.VERSION};
    }

    void run() {
        println "run base"
        // def args = ConfigEnv.instance.commandArgs
        // args.configFile = configFile
        // args.password = password
    }

    static void main(String[] args) {
        CommandLine cmd = new CommandLine(new GetconfigCommand())
        cmd.execute(args)
    }

    @Command(name = "init", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "generate project directory")
    static class InitCommand implements Callable<Integer> {

        @Option(names = ["-d", "--dryrun"], 
                description="generate project dirdectory with test")
        Boolean dryRun

        @Option(names = ["-t", "--tutorial"], 
                description="add test data for tutorial")
        Boolean tutorial

        @Parameters(paramLabel = "DIR", description = "project directory to init")
        String projectDir;

        @Override
        public Integer call() {
            try {
                def env = ConfigEnv.instance
                env.commandArgs.copyPropeties(this)
//                ProjectManager manager = new ProjectManager()
                ProjectInitializer manager = new ProjectInitializer()
                env.accept(manager)
                String mode = (tutorial) ? 'detail' : 'normal'
                manager.initProject(projectDir, mode)
            } catch (Exception e) {
                log.error "run command : " + e
                return ExitCode.SOFTWARE
            }
            return ExitCode.OK;
        }
    }

    @Command(name = "backup", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "backup zip from project source")
    static class BackupCommand implements Callable<Integer> {

        @Option(names = ["-a", "--archive"], paramLabel="FILE", 
                description="archive project zip file")
        File zipPath

        @Option(names = ["-f", "--force"], description="Force backup without checking the integrity of the project directory")
        Boolean force

        @Parameters(paramLabel = "DIR", description = "project directory to backup")
        String projectDir

        @Override
        public Integer call() {
            try {
                def env = ConfigEnv.instance
                env.commandArgs.copyPropeties(this)
                BackupManager manager = new BackupManager()
                env.accept(manager)
                manager.backupProject(zipPath, projectDir, force)
            } catch (Exception e) {
                log.error "run command : " + e
                return ExitCode.SOFTWARE
            }
            return ExitCode.OK;
        }
    }

    @ToString
    @Command(name = "run", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "run inventory collector")
    static class RunCommand implements Callable<Integer> {

        @Option(names = ["-c", "--config"], paramLabel = "FILE",
                description = "config file path (default : config/config.groovy)")
        File configFile

        @Option(names = ["-p", "--password"], 
                description = "config file password")
        String password

        @Option(names = ["--level"], description = "test item filtering level(default : 0)")
        Integer level

        @Option(names = ["-a", "--auto-tag"], description = "use auto tag mode")
        Boolean autoTagFlag

        @Option(names = ["-an", "--auto-tag-number"], paramLabel = "COUNT",
                description = "use auto tag mode with the count")
        Integer autoTagNumber

        @Option(names = ["-d", "--dryrun"], 
                description="dry run test")
        Boolean dryRun

        @Option(names = ["-x", "--skip-excel"], 
                description="skip excel report")
        Boolean skipExcelReport

        @Option(names = ["-e", "--excel"], paramLabel="FILE", 
                description="check sheet input (default: check_sheet.xlsx)")
        File checkSheetPath

        @Option(names = ["-o", "--output"], paramLabel="FILE", 
                description="evidence sheet output (default: build/check_sheet.xlsx)")
        File evidenceSheetPath

        @Option(names = ["-s", "--server"], description = "keyword of filtering server")
        String keywordServer

        @Option(names = ["-t", "--test"], description = "keyword of filtering platform")
        String keywordPlatform

        @Option(names = ["--silent"], description="silent mode")
        Boolean silent

        @Override
        public Integer call() {
            // try {
                def env = ConfigEnv.instance
                env.commandArgs.copyPropeties(this)
                env.readConfig()
                TestRunner runner = new TestRunner()
                env.accept(runner)
                runner.run()
            // } catch (Exception e) {
            //     log.error "run command : " + e
            //     return ExitCode.SOFTWARE
            // }
            return ExitCode.OK
        }
    }

    // ToDo: make spec encode/demode options.
    @Command(name = "encode", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "encode config file")
    static class EncodeCommand implements Callable<Integer> {

        @Option(names = ["-c", "--config"], paramLabel = "FILE",
                description = "config file path (default : config/config.groovy)")
        File configFile

        @Option(names = ["-p", "--password"], 
                description = "config file password")
        String password

        @Option(names = ["-r", "--restore"], 
                description = "decode config file")
        Boolean restore

        @Override
        public Integer call() {
            try {
                def env = ConfigEnv.instance
                env.commandArgs.copyPropeties(this)
                ConfigFileEncoder manager = new ConfigFileEncoder()
                env.accept(manager)
                if (restore) {
                    manager.restore()
                } else {
                    manager.run()
                }
            } catch (Exception e) {
                log.error "run command : " + e
                return ExitCode.SOFTWARE
            }
            return ExitCode.OK;
        }
    }

    // @Command(name = "ls", mixinStandardHelpOptions = true,
    //         versionProvider = GetconfigCommand.class,
    //         description = "list inventory")
    // static class ListCommand implements Callable<Integer> {

    //     @Option(names = ["-a", "--all"], description="print all inventory list")
    //     boolean allFlag

    //     @Option(names = ["-s", "--server"], description = "keyword of filtering server")
    //     String keywordServer

    //     @Option(names = ["-p", "--platform"], description = "keyword of filtering platform")
    //     String keywordPlatform

    //     @Override
    //     public Integer call() {
    //         ConfigEnv.instance.commandArgs.copyPropeties(this)

    //         println("list");
    //         return ExitCode.OK;
    //     }
    // }

    @Command(name = "update", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "update inventory data")
    static class UpdateCommand implements Callable<Integer> {

        @Option(names = ["-c", "--config"], paramLabel = "FILE",
                description = "config file path (default : config/config.groovy)")
        File configFile

        @Option(names = ["-p", "--password"], 
                description = "config file password")
        String password

        @Option(names = ["-d", "--dryrun"], 
                description="generate project dirdectory with test")
        Boolean dryRun

        @Option(names = ["-s", "--show-config"], 
                description="print database config parameter")
        Boolean showConfig

        @Option(names = ["-e", "--excel"], paramLabel="FILE", 
                description="check sheet input (default: check_sheet.xlsx)")
        File checkSheetPath

        @Option(names = ["-r", "--redmine"], description = "redmine project")
        String redmineProject

        @Parameters(paramLabel = "[local|db|ticket|all]", defaultValue = "local",
                description = "update inventory database")
        String targetType = "local"

        @Override
        public Integer call() {
            try {
                def env = ConfigEnv.instance
                env.commandArgs.copyPropeties(this)
                env.readConfig()
                Exporter exporter = new Exporter()
                env.accept(exporter)
                if (this.showConfig) {
                    exporter.showConfig()
                }else {
                    exporter.run()
                }

                // ProjectManager manager = new ProjectManager()
                // env.accept(manager)
                // manager.update(targetType)
            } catch (Exception e) {
                log.error "update command : " + e
                return ExitCode.SOFTWARE
            }
            return ExitCode.OK

            // def args = ConfigEnv.instance.commandArgs
            // args.configFile = configFile
            // args.password = password
            // args.targetType = targetType
            // println("update");
            // return ExitCode.OK;
        }
    }

    // @Command(name = "regist", mixinStandardHelpOptions = true,
    //         versionProvider = GetconfigCommand.class,
    //         description = "regist redmine inventory ticket")
    // static class RegistCommand implements Callable<Integer> {

    //     @Option(names = ["-c", "--config"], paramLabel = "FILE",
    //             description = "config file path (default : config/config.groovy)")
    //     File configFile

    //     @Option(names = ["-p", "--password"], 
    //             description = "config file password")
    //     String password

    //     @Option(names = ["--project"], description = "redmine project")
    //     String project

    //     @Override
    //     public Integer call() {
    //         def args = ConfigEnv.instance.commandArgs
    //         args.configFile = configFile
    //         args.password = password
    //         args.redmineProject = project
    //         println("regist");
    //         return ExitCode.OK;
    //     }
    // }
}
