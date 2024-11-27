package com.getconfig

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
@ToString
class ConfigCommandArgs {
    // 共通設定
    File configFile
    String password

    // run サブコマンド用
    Boolean dryRun
    Boolean skipExcelReport
    String projectDir
    Integer level
    Boolean autoTagFlag
    Integer autoTagNumber
    File checkSheetPath
    File evidenceSheetPath
    String keywordServer
    String keywordTest
    String keywordPlatform
    Boolean silent

    // backup サブコマンド
    File zipPath

    // ls サブコマンド用
    Boolean allFlag

    // update サブコマンド用
    String targetType

    // regist サブコマンド用
    String redmineProject

    void copyPropeties(Object source) {
        def props = (Map<String, Object>) source.properties
        props.each { entry ->
            if (this.hasProperty(entry.key) && !(entry.key in ['class', 'metaClass']))
                this[entry.key] = entry.value
        }
    }
}
