package com.getconfig.Document

import com.getconfig.Model.Result
import com.getconfig.Model.ResultTag
import com.getconfig.Model.TestScenario
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
class ResultComparator {
    TestScenario testScenario

    ResultComparator(TestScenario testScenario) {
        this.testScenario = testScenario
    }

    void run() {
        testScenario.metricIndex.each { String metricId, String metricKey ->
            testScenario.serverGroupTags.entries().each {
                String tagServer = it.getKey()
                String server = it.getValue()
                ResultTag resultTag = testScenario.resultTags.get(tagServer, metricId)
                if (!resultTag) {
                    resultTag = new ResultTag()
                    testScenario.resultTags.put(tagServer, metricId, resultTag)
                }
                Result result = testScenario.results.get(server, metricId)
                Result resultCompare = testScenario.results.get(tagServer, metricId)
                if (result) {
                    result.comparison = Result.ResultStatus.UNMATCH
                    if (resultCompare) {
                        if (result.value == resultCompare.value) {
                            result.comparison = Result.ResultStatus.MATCH
                        }
                    }
                    resultTag.evalComparisonCounter(result.comparison)
                }
            }
        }
        // println testScenario.resultTags
    }
}
