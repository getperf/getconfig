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

    boolean used() {
        return (matchCount != 0 ||  unMatchCount != 0)
    }

    void countMatchCounter(boolean comparison) {
         if (comparison) {
             this.countMatch()
         } else {
             this.countUnMatch()
         }
    }

    void evalComparisonCounter(Result.ResultStatus resultStatus) {
         this.countMatchCounter(resultStatus == Result.ResultStatus.MATCH)
    }
}
