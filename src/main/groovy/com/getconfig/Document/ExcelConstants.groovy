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

    // 既定のセルスタイルID
    public static final String DEFAULT_CELL_STYLE_ID = "Normal";

    // テンプレートのサマリレポートシート名
    public static final String TEMPLATE_SUMMARY_SHEET_NAME = "Summary";

    // サマリレポートシート名
    public static final String SUMMARY_SHEET_NAME = "サマリレポート";

    // テンプレートの検査結果シート名
    public static final String TEMPLATE_RESULT_SHEET_NAME = "TestResult";

    // 検査結果シート名
    public static final String RESULT_SHEET_NAME = "検査結果";

    // テンプレートのデバイスシート名
    public static final String TEMPLATE_DEVICE_SHEET_NAME = "Device";

    // デバイスシート名
    public static final String DEVICE_SHEET_NAME = "検査詳細";
}
