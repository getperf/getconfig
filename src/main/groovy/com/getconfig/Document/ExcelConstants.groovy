package com.getconfig.Document

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class ExcelConstants {
    // フォント名
    public static final String FONT = "ＭＳ Ｐゴシック";

    // セルスタイルシート名
    public static final String CELL_STYLE_SHEET_NAME = "CellStyle";

    // 非該当項目のセル値
    public static final String CELL_NOT_APPLICABLE_VALUE = "-";

    // セル欠損値
    public static final String CELL_NOT_UNKOWN_VALUE = "(不明)";

    // 既定のセルスタイルID
    public static final String DEFAULT_CELL_STYLE_ID = "Normal";

    // 検査仕様書の検査対象リストのシート名
    public static final String TEST_SERVER_SHEET_NAME = "検査対象";

    // 検査仕様書の検査対象リストのヘッダー行
    public static final int TEST_SERVER_SHEET_HEADER_ROW = 2;

    // 検査仕様書のパラメータシート名プレフィックス
    public static final String TEST_PARAMETER_SHEET_NAME_PREFIX = "パラメータ";

    // テンプレートのサマリレポートシート名
    public static final String TEMPLATE_SUMMARY_SHEET_NAME = "Summary";

    // サマリレポートシート名
    public static final String SUMMARY_SHEET_NAME = "サマリレポート";

    // テンプレートの検査結果シート名
    public static final String TEMPLATE_RESULT_SHEET_NAME = "TestResult";

    // 検査結果シート名
    public static final String RESULT_SHEET_NAME = "検査結果";

    // 検査結果シートのプラットフォーム種別表示順
    public static final List<String> RESULT_SHEET_PLATFORM_CATEGORY_ORDER =
            ["HW", "OS", "MONITOR", "MW"]
    // テンプレートのデバイスシート名
    public static final String TEMPLATE_DEVICE_SHEET_NAME = "Device";

    // デバイスシート名
    public static final String DEVICE_SHEET_NAME = "検査詳細";

    // 検査結果レポート定義ファイル名
    public static final String REPORT_RESULT_CONFIG_TOML = "report_result.toml";

    // サマリレポート定義ファイル名
    public static final String REPORT_SUMMARY_CONFIG_TOML = "report_summary.toml";

    // サマリレポート行の高さ
    public static final int REPORT_SUMMARY_ROW_HEIGHT = 50;
}
