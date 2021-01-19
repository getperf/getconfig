package com.getconfig

import com.getconfig.Document.ExcelConstants
import com.getconfig.Document.ReportMaker
import com.getconfig.Document.ReportMakerDevice
import com.getconfig.Document.ReportMakerResult
import com.getconfig.Document.ReportMakerSummary
import com.getconfig.Document.ResultComparator
import com.getconfig.Document.SheetManager
import com.getconfig.Document.TagGenerator
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
    Boolean autoTagFlag = false
    int autoTagNumber = 1

    Reporter(TestScenario testScenario, String reportPath = null) {
        this.testScenario = testScenario
        this.reportPath = reportPath
    }

    void setEnvironment(ConfigEnv env) {
        this.reportPath = env.getEvidenceSheetPath()
        this.autoTagFlag = env.getAutoTagFlag()
        this.autoTagNumber = env.getAutoTagNumber()
    }

    void makeAutoTag() {
        TagGenerator tagGenerator = new TagGenerator(testScenario, autoTagNumber)
        tagGenerator.run()
    }

    void compareData() {
        ResultComparator resultComparator = new ResultComparator(testScenario)
        resultComparator.run()
    }

    void initReport() {
        reportMaker = new ReportMaker(excelTemplate).read()
    }

    void makeSummaryReport() {
        long start = System.currentTimeMillis()
        new ReportMakerSummary(testScenario, reportMaker).make()
        long elapse = System.currentTimeMillis() - start
        log.info "create summary sheet elapse : ${elapse} ms"
    }

    void makeResultReport() {
        long start = System.currentTimeMillis()
        new ReportMakerResult(testScenario, reportMaker).make()
        long elapse = System.currentTimeMillis() - start
        log.info "create result sheet elapse : ${elapse} ms"
    }

    void makeDeviceReport() {
        long start = System.currentTimeMillis()
        new ReportMakerDevice(testScenario, reportMaker).make()
        long elapse = System.currentTimeMillis() - start
        log.info "create device sheet elapse : ${elapse} ms"
    }

    void finishReport() {
        reportMaker.finishSheetManager()
        reportMaker.finish()
        reportMaker.write(this.reportPath)
    }

    int run() {
        if (autoTagFlag) {
            makeAutoTag()
        }
        compareData()
        initReport()
        makeSummaryReport()
        makeResultReport()
        makeDeviceReport()
        finishReport()
        log.info "finish '${reportPath}' saved"
        return 0
    }

}
