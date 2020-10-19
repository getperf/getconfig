package com.getconfig


import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Slf4j
@CompileStatic(TypeCheckingMode.SKIP)
@Singleton
class ConfigEnv {
    final static String DefaultDateFormat = "yyyyMMdd_HHmmss"
    final static int DefaultGconfTimeout = 120
    final static int DefaultAutoTagNumber = 10

    ConfigObject config
    ConfigCommandArgs commandArgs = new ConfigCommandArgs()

    final static String accountNotFound = "account not found in config.groovy"

    void readConfig(String configFile = null, String keyword = null) {
        this.config = Config.instance.readConfig(
                configFile ?: this.getConfigFile() as String,
                keyword ?: this.getPassword())
        convertDateFormat()
    }

    void convertDateFormat() {
        def localDateTime = LocalDateTime.now()
        def formatter = DateTimeFormatter.ofPattern(DefaultDateFormat)
        def nowLabel = localDateTime.format(formatter)
        def evidence = (Map<String, GString>) this.config?.evidence
        if (evidence) {
            evidence.each { key, value ->
                evidence[key] = value.replaceAll(/<date>/, nowLabel)
            }
        }
    }

    void setAccount(Server sv) {
        if (sv.accountId.size() == 0 || sv.domain.size() == 0) {
            return
        }
        ConfigObject accounts = this.config.get("account")
        if (!accounts) {
            throw new IllegalArgumentException(accountNotFound)
        }
        ConfigObject account = accounts.get(sv.domain)?.get(sv.accountId)
        if (!account) {
            String parameter = "account.${sv.domain}.${sv.accountId}"
            throw new IllegalArgumentException("${accountNotFound} : ${parameter}")
        }
        sv.setAccount(account)
    }

    void  setDryRun(String platform = "Default") {
        this.config.test.dry_run[platform] = true
    }

    boolean isWindows() {
        String osName = System.properties['os.name']
        return (osName.toLowerCase().contains('windows'))
    }

    void accept(Controller visitor) {
        visitor.setEnvironment(this)
    }
    
    // インストールディレクトリ
    String getGetconfigHome() {
        return this.config?.getconfig_home ?: System.getProperty("getconfig_home") ?: '.'
    }

    // プロジェクトホームディレクトリ
    String getProjectHome() {
        return this.config?.project_home ?: System.getProperty("user.dir")
    }

    // プロジェクト名
    String getProjectName() {
        return new File(this.getProjectHome()).getName()
    }

    // gconf 実行パス
    String getGconfExe() {
        String gconfExe = (this.isWindows()) ? "gconf.exe" : "gconf"
        return Paths.get(this.getGetconfigHome(), gconfExe)
    }

    // getconfig 実行パス
    String getGetconfigExe() {
        String getconfigExe = (this.isWindows()) ? "getconfig.bat" : "getconfig"
        return Paths.get(this.getGetconfigHome(), getconfigExe)
    }

    // 検査シートパス  チェックシート.xslx
    String getCheckSheetPath() {
        return this.commandArgs.checkSheetPath ?:
                this.config?.get('evidence')?.get('source') ?:
                        Paths.get(this.getProjectHome(), 'getconfig.xlsx')
    }

    // プロジェクトログディレクトリ   src/test/resources/log
    String getProjectLogDir() {
        return Paths.get(this.getProjectHome(), 'src/test/resources/inventory')
    }

    // 構成管理DB設定パス  config/cmdb.groovy
    String getCmdbConfigPath() {
        return this.config?.db_config ?:
                Paths.get(this.getGetconfigHome(), "config/cmdb.groovy")
    }

    // エージェント設定ファイル用ディレクトリ  build/gconf
    String getAgentConfigDir() {
        return this.config?.gconf_config_dir ?:
                Paths.get(this.getProjectHome(), "build/gconf")
    }

    // エージェントラッパーライブラリパス  lib/gconf
    // String getAgentWrapperLib() {
    //     return this.config?.gconf_wrapper_lib ?:
    //             Paths.get(this.getProjectHome(), "lib/agentconf")
    // }

    // メトリックライブラリパス  lib/dictionary
    String getMetricLib() {
        return this.config?.metric_lib ?:
                Paths.get(this.getProjectHome(), "lib/dictionary")
    }

    // メトリックパーサーライブラリパス  lib/parser
    String getAgentLogParserLib() {
        return this.config?.agent_log_parser_lib ?:
                Paths.get(this.getProjectHome(), "lib/parser")
    }

    // TLS証明書用ディレクトリ  config/network
    String getTlsConfigDir() {
        return this.config?.gconf_tls_config ?:
                System.getProperty("ptune_network_config") ?:
                Paths.get(this.getProjectHome(), "config/network")
    }

    // 検査結果    buikd/チェックシート_<date>.xlsx
    String getEvidenceSheetPath() {
        return this.config?.output_evidence ?:
                this.config?.evidence?.target ?:
                        Paths.get(this.getProjectHome(), "build/check_sheet.xlsx")
    }

    // 一時ログディレクトリ  /build/log
    String getCurrentLogDir() {
        return Paths.get(this.getProjectHome(), "build/log")
    }

    // 一時ノード定義ディレクトリ  /build/node
    String getCurrentNodeDir() {
        return Paths.get(this.getProjectHome(), "build/node")
    }

    // プロジェクトノード定義ディレクトリ  /node
    String getProjectNodeDir() {
        return Paths.get(this.getProjectHome(), "node")
    }

    // ベースノード定義ディレクトリ  {home}/node
    String getBaseNodeDir() {
        return Paths.get(this.getGetconfigHome(), "node")
    }

    // 検査スクリプト /lib/InfraTestSpec
    String getTestSpecDir() {
        return Paths.get(this.getProjectHome(), "node")
    }

    // gconf エージェントコマンドタイムアウト
    int getGconfTimeout(String platform = "Default") {
        def timeouts = this.config?.test?.timeout as Map<String, Integer>
        if (timeouts) {
            return timeouts[platform] ?: DefaultGconfTimeout
        } else {
            return DefaultGconfTimeout
        }
    }

    boolean  getDryRun(String platform = "Default") {
        boolean dryRun = false
        def dryRuns = this.config?.test?.dry_run as Map<String, Integer>
        if (dryRuns) {
            dryRun = dryRuns[platform] ?: false
        }
        return this.commandArgs.dryRun ?: dryRun
    }

    // 共通設定
    String getConfigFile() {
        def configFile = Paths.get(this.getProjectHome(), 'config/config.groovy')
        return this.commandArgs.configFile ?: configFile
    }

    String getPassword() {
        return this.commandArgs.password
    }

    int getLevel() {
        return this.commandArgs.level ?: 0
    }

    boolean getAutoTagFlag() {
        return this.commandArgs.autoTagFlag ?: false
    }

    int getAutoTagNumber() {
        return this.commandArgs.autoTagNumber ?: DefaultAutoTagNumber
    }
    String getKeywordServer() {
        return this.commandArgs.keywordServer
    }

    String getKeywordTest() {
        return this.commandArgs.keywordTest
    }

    String getKeywordPlatform() {
        return this.commandArgs.keywordPlatform
    }

    boolean getSilent() {
        return this.commandArgs.silent ?: false
    }

    String getZipPath() {
        return this.commandArgs.zipPath
    }

    // ls サブコマンド用
    boolean getAllFlag() {
        return this.commandArgs.allFlag ?: true
    }

    // update サブコマンド用
    String getTargetType() {
        return this.commandArgs.targetType
    }

    // regist サブコマンド用
    String getRedmineProject() {
        return this.commandArgs.redmineProject
    }

    // 検査シートパス  チェックシート.xslx
    String getHubInventoryDir() {
        return this.config?.get('test')?.get('hub_inventory_dir') ?:
                        Paths.get(this.getGetconfigHome(),
                                'src/test/resources/hub/inventory')
    }

}
