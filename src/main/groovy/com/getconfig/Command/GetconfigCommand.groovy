package com.getconfig.Command

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.ExitCode
import picocli.CommandLine.IFactory
import picocli.CommandLine.ParameterException
import picocli.CommandLine.ParseResult
import picocli.CommandLine.IVersionProvider
import picocli.CommandLine.IExitCodeExceptionMapper

import java.math.BigDecimal
import java.util.Arrays
import java.util.Optional
import java.util.concurrent.Callable

import com.getconfig.TestRunner
import com.getconfig.Document.SpecReader

@CompileStatic
@Command(name = "getconfig", mixinStandardHelpOptions = true,
        versionProvider = GetconfigCommand.class,
        description = "Getconfig inventory collector",
        subcommands = [
                GetconfigCommand.InitCommand.class,
                GetconfigCommand.RunCommand.class,
                GetconfigCommand.BackupCommand.class,
                GetconfigCommand.ListCommand.class,
                GetconfigCommand.UpdateCommand.class,
                GetconfigCommand.RegistCommand.class,
                GetconfigCommand.EncriptCommand.class
        ])
public class GetconfigCommand implements Callable<Integer>, IVersionProvider, IExitCodeExceptionMapper {

    // picocli.AutoComplete で generate Completion Script をする時に引数なしのコンストラクタが必要になる
    // のでコンストラクタインジェクションは使用しないこと
    // https://picocli.info/autocomplete.html 参照
    // @Autowired
    // private BuildProperties buildProperties;

    @Override
    public Integer call() {
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
        return new String[]{"Groovy picocli demo v4.1"};
    }

    @Option(names = ["-c", "--config"], paramLabel = "FILE",
            description = "config file path (default : config/config.groovy)")
    File configFile

    @Option(names = ["-p", "--password"], 
            description = "config file password")
    String password

    void run() {
        // count.times {
        //     println("hello world $it...")
        // }
    }

    static void main(String[] args) {
        CommandLine cmd = new CommandLine(new GetconfigCommand())
        cmd.execute(args)
    }

    // @Command(name = "add", mixinStandardHelpOptions = true,
    //         versionProvider = GetconfigCommand.class,
    //         description = "渡された数値を加算する")
    // static class AddCommand implements Callable<Integer> {

    //     @Option(names = ["-a", "--avg"], description = "平均値を算出する")
    //     private boolean optAvg;

    //     @Parameters(paramLabel = "数値", arity = "1..*", description = "加算する数値")
    //     private BigDecimal[] nums;

    //     @Override
    //     public Integer call() {
    //         BigDecimal sum =
    //                 Arrays.asList(nums).stream()
    //                         .reduce(new BigDecimal("0"), (a, v) -> a.add(v));
    //         Optional<BigDecimal> avg = optAvg
    //                 ? Optional.of(sum.divide(BigDecimal.valueOf(nums.length)))
    //                 : Optional<BigDecimal>.empty();
    //         System.out.println(avg.orElse(sum));
    //         return ExitCode.OK;
    //     }
    // }

    // @Command(name = "multi", mixinStandardHelpOptions = true,
    //         versionProvider = GetconfigCommand.class,
    //         description = "渡された数値を乗算する")
    // static class MultiCommand implements Callable<Integer> {

    //     @Parameters(paramLabel = "数値", arity = "1..*", description = "乗算する数値")
    //     private BigDecimal[] nums;

    //     @Option(names = ["-c", "--compare"],
    //             description = "計算結果と比較して、計算結果 < 数値なら -1、計算結果 = 数値なら 0、計算結果 > 数値なら 1 を返す")
    //     private BigDecimal compareNum;

    //     @Override
    //     public Integer call() {
    //         BigDecimal result =
    //                 Arrays.asList(nums).stream()
    //                         .reduce(new BigDecimal("1"), (a, v) -> a.multiply(v));
    //         Optional<Integer> compareResult = (compareNum == null)
    //                 ? Optional<Integer>.empty()
    //                 : Optional.of(result.compareTo(compareNum));
    //         System.out.println(compareResult.isPresent() ? compareResult.get() : result);
    //         return ExitCode.OK;
    //     }
    // }

    @Command(name = "init", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "generate project directory")
    static class InitCommand implements Callable<Integer> {

        @Option(names = ["-d", "--dryrun"], 
                description="generate project dirdectory with test")
        private boolean dryRun

        @Parameters(paramLabel = "DIR", description = "project directory to init")
        private String projectDir;

        @Override
        public Integer call() {
            println("init ${projectDir}, ${dryRun}");
            return ExitCode.OK;
        }
    }

    @Command(name = "backup", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "backup zip from project source")
    static class BackupCommand implements Callable<Integer> {

        @Option(names = ["-a", "--archive"], paramLabel="FILE", 
                description="archive project zip file")
        private File zipPath

        @Parameters(paramLabel = "DIR", description = "project directory to backup")
        private String projectDir

        @Override
        public Integer call() {
            println "backup ${zipPath}, ${projectDir}"
            return ExitCode.OK
        }
    }

    @Command(name = "run", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "run inventory collector")
    static class RunCommand implements Callable<Integer> {

        @Option(names = ["--level"], description = "test item filtering level(default : 0)")
        private int level = 0

        @Option(names = ["-a", "--auto-tag"], description = "use auto tag mode")
        private boolean autoTagFlag

        @Option(names = ["-an", "--auto-tag-number"], paramLabel = "COUNT",
                description = "use auto tag mode with the count")
        private int autoTagNumber = 0

        @Option(names = ["-d", "--dryrun"], 
                description="dry run test")
        private boolean dryRun

        @Option(names = ["-e", "--excel"], paramLabel="FILE", 
                description="check sheet input (default: check_sheet.xlsx)")
        private File checkSheetPath

        @Option(names = ["-o", "--output"], paramLabel="FILE", 
                description="evidence sheet output (default: build/check_sheet.xlsx)")
        private File evidenceSheetPath

        @Option(names = ["-s", "--server"], description = "keyword of filtering server")
        private String keywordServer

        @Option(names = ["-t", "--test"], description = "keyword of filtering test")
        private String keywordTest

        @Option(names = ["--silent"], description="silent mode")
        private boolean slient

        @Override
        public Integer call() {
            println "Run"
            // TestRunner runner = new TestRunner()
            // runner.run()
            SpecReader reader = new SpecReader()
            reader.run()
            return ExitCode.OK
        }
    }

    // ToDo: make spec encode/demode options.
    @Command(name = "encript", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "encript config file")
    static class EncriptCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            println("encript");
            return ExitCode.OK;
        }
    }

    @Command(name = "ls", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "list inventory")
    static class ListCommand implements Callable<Integer> {

        @Option(names = ["-a", "--all"], description="print all inventory list")
        private boolean allFlag

        @Option(names = ["-s", "--server"], description = "keyword of filtering server")
        private String keywordServer

        @Option(names = ["-p", "--platform"], description = "keyword of filtering platform")
        private String keywordPlatform

        @Override
        public Integer call() {
            println("list");
            return ExitCode.OK;
        }
    }

    @Command(name = "update", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "update inventory data")
    static class UpdateCommand implements Callable<Integer> {

        @Parameters(paramLabel = "[local|db|db-all]", description = "update inventory database")
        private String targetType = "local"

        @Override
        public Integer call() {
            println("update");
            return ExitCode.OK;
        }
    }

    @Command(name = "regist", mixinStandardHelpOptions = true,
            versionProvider = GetconfigCommand.class,
            description = "regist redmine inventory ticket")
    static class RegistCommand implements Callable<Integer> {

        @Option(names = ["-p", "--project"], description = "redmine project")
        private String project

        @Override
        public Integer call() {
            println("regist");
            return ExitCode.OK;
        }
    }
}
