package com.getconfig.Model
import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ResultTag {
    int matchCount = 0
    int unMatchCount = 0

    int countMatch() {
        return matchCount ++
    }

    int countUnMatch() {
        return unMatchCount ++
    }

    double rate() {
        if (matchCount + unMatchCount == 0) {
            return 0
        }
        return (double)matchCount / (matchCount + unMatchCount)
    }

    void evalComparisonCounter(Result.ResultStatus resultStatus) {
         if (resultStatus == Result.ResultStatus.MATCH) {
             this.countMatch()
         } else {
             this.countUnMatch()
         }

    }
}
