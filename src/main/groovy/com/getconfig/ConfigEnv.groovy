package com.getconfig

import groovy.io.FileType
import groovy.transform.*
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import java.nio.file.Paths 

import com.getconfig.Model.TestServer
import com.getconfig.GconfWrapper.GconfWrapper

@Slf4j
@CompileStatic(TypeCheckingMode.SKIP)
@Singleton
class ConfigEnv {
    String home
    String configFile
    ConfigObject config
    Map<String, GconfWrapper> gconfWrappers = new LinkedHashMap<String, GconfWrapper>()

    static final String accountNotFound = "account not found in config"

    void readConfig(String configFile, String keyword = null) {
        this.configFile = configFile
        this.config = Config.instance.readConfig(configFile, keyword)
    }

    void setAccont(TestServer sv) {
        if (sv.accountId.size() == 0 || sv.domain.size() == 0) {
            return
        }
        def accounts = this.config.get("account")
        if (!accounts) {
            throw new IllegalArgumentException(accountNotFound)
        }
        ConfigObject account = accounts?.get(sv.domain)?.get(sv.accountId)
        if (!account) {
            throw new IllegalArgumentException(accountNotFound)
        }
        sv.setAccont(account)
    }

    boolean isWindows() {
        String osName = System.properties['os.name']
        return (osName.toLowerCase().contains('windows'))
    }

    void loadGconfWrapper() {
        String userLib = this.getGconfWrapperLib()
        new File(userLib).eachFileMatch(FileType.FILES, ~/\w+.groovy/) { source ->
            // def className = source.name 
            def loader = new GroovyClassLoader()
            loader.addClasspath(userLib)
            loader.clearCache()
            def code = source.getText('UTF-8')
            try {
                def clazz = loader.parseClass(code)
                def className = clazz.name.replaceFirst(/.+\./, "")
                this.gconfWrappers[className] = clazz
            } catch (Exception e) {
                log.warn "Read error : ${source} :" + e
            }
        }
        // Match(FileType.FILES, ~/groovy/) { println it.name }

        // def user_script = "${user_lib}/${user_package}/${test_platform.name}Spec.groovy"
        // log.debug "Load ${user_script}"
        // def clazz = test_config.test_specs[user_script]
        // if (!(clazz)) {
        //     log.info "Load script '${user_script}'"
        //     long start = System.currentTimeMillis()
        //     def loader = new GroovyClassLoader()
        //     loader.addClasspath(user_lib)
        //     loader.clearCache()
        //     def code = new File(user_script).getText('UTF-8')
        //     clazz = loader.parseClass(code)
        //     test_config.test_specs[user_script] = clazz
        //     long elapse = System.currentTimeMillis() - start
        //     log.info "Finish Load script, Elapse : ${elapse} ms"
        // }

    }
    // * getconfig[.bat]
    // * gconf[.exe]
    // * チェックシート.xlsx  検査シート
    // * config/
    //     * config.groovy  設定ファイル
    //     * cmdb.groovy 構成管理DB設定
    //     * network/{server}.key  キーファイル <新規>
    // * build/
    //     * チェックシート_<date>.xlsx  実行結果
    //     * lib/getconfig-0.2.xx-all.jar
    //     * log/  コマンド実行結果
    //     * json/
    //         * {server}__{platform}.json IP集計結果
    //         * {server}/{platform}.json 集計結果
    //     * gconf/{platform}.toml <新規>
    // * lib/
    //     * InfraTestSpec/{platform}.groovy 検査スクリプト
    //     * script/*.groovy   ツール
    //     * template/*.template   PowerShell スクリプトテンプレート
    // * src/test/resources/log/
    //     * ドライラン用ログ保存ディレクトリ
    // * node/ --- build/json のコピー

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
        String gconfExe = (this.isWindows())?"gconf.exe":"gconf"
        return Paths.get(this.getGetconfigHome(), gconfExe)
    }

    // getconfig 実行パス
    String getGetconfigExe() {
        String getconfigExe = (this.isWindows())?"getconfig.bat":"getconfig"
        return Paths.get(this.getGetconfigHome(), getconfigExe)
    }

    // 検査シートパス  チェックシート.xslx
    String getCheckSheetPath() {
        return this.config?.excel_file ?:
               this.config?.get('evidence')?.get('source') ?:
               Paths.get(this.getProjectHome(), 'check_sheet.xlsx')
    }

    // プロジェクトログディレクトリ   src/test/resources/log
    String getProjectLogDir() {
        return Paths.get(this.getProjectHome(), 'src/test/resources/log')
    }

    // 設定ファイル   config/config.groovy
    String getConfigPath() {
        return this.configFile
    }

    // 構成管理DB設定パス  config/cmdb.groovy
    String getCmdbConfigPath() {
        return this.config?.db_config ?: 
               Paths.get(this.getGetconfigHome(), "config/cmdb.groovy")
    }

    // gconf設定ファイル用ディレクトリ  build/gconf
    String getGconfConfigDir() {
        return this.config?.gconf_config_dir ?: 
               Paths.get(this.getProjectHome(), "build/gconf")
    }

    // gconf ラッパー用ディレクトリ  lib/gconf
    String getGconfWrapperLib() {
        return this.config?.gconf_wrapper_lib ?: 
               Paths.get(this.getProjectHome(), "lib/gconf")

    }

    // TLS証明書用ディレクトリ  config/network
    String getTlsConfigDir() {
        return this.config?.db_config ?: 
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
}
