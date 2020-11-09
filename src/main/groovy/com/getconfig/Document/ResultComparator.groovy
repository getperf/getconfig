package com.getconfig.Document

import com.getconfig.Model.Result
import com.getconfig.Model.ResultTag
import com.getconfig.Model.TestScenario
import com.google.common.collect.Table
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
        Table<String, String, Result> results
            = testScenario.results
        Table<String, String, ResultTag> resultTags
            = testScenario.resultTags

        testScenario.metricIndex.each {
            String metricId, String metricKey ->
            testScenario.serverGroupTags.entries().each {
                String tagServer = it.getKey()
                String testServer = it.getValue()
                ResultTag tag = resultTags.get(tagServer, metricId)
                if (!tag) {
                    tag = new ResultTag()
                    resultTags.put(tagServer, metricId, tag)
                }
                Result testResult = results.get(testServer, metricId)
                Result tagResult = results.get(tagServer, metricId)
                boolean comparison = false
                if (testResult && tagServer != testServer) {
                    // println "tag:$tagServer,test:$testServer,metric:$metricId,res:$testResult"
                    testResult.compareValue(tagResult)
                    // comparison = (testResult.comparison == Result.ResultStatus.MATCH)
                    comparison = testResult.isMatch()
                }
                if (tagServer != testServer && (tagResult || testResult)) {
                    tag.countMatchCounter(comparison)
                }
            }
        }
        // println testScenario.resultTags
    }
}
