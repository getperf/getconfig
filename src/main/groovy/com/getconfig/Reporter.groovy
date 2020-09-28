package com.getconfig

import com.getconfig.Document.ExcelConstants
import com.getconfig.Document.ReportMaker
import com.getconfig.Document.ReportMakerResult
import com.getconfig.Document.ReportMakerSummary
import com.getconfig.Document.SheetManager
import com.getconfig.Model.Metric
import com.getconfig.Model.MetricId
import com.getconfig.Model.PlatformMetric
import com.getconfig.Model.ReportSummary
import com.getconfig.Model.Result
import com.getconfig.Model.ResultSheet
import com.getconfig.Model.TestScenario
import com.getconfig.Utils.TomlUtils
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class Reporter implements Controller {
    TestScenario testScenario
    ReportMaker reportMaker
    String reportPath
    String excelTemplate = "lib/template/report_summary.xlsx"

    Reporter(TestScenario testScenario, String reportPath = null) {
        this.testScenario = testScenario
        this.reportPath = reportPath
    }

    void setEnvironment(ConfigEnv env) {
        this.reportPath = env.getEvidenceSheetPath()
    }

    void compareData() {
    }

    void initReport() {
        reportMaker = new ReportMaker(excelTemplate).read()
    }

    void makeSummaryReport() {
        new ReportMakerSummary(testScenario, reportMaker).make()
    }

    void makeResultReport() {
        new ReportMakerResult(testScenario, reportMaker).make()
    }

    void finishReport() {
        reportMaker.finishSheetManager()
        reportMaker.finish()
        reportMaker.write(this.reportPath)
    }

    int run() {
        compareData()
        initReport()
        makeSummaryReport()
        makeResultReport()
        finishReport()

        return 0
    }

}
