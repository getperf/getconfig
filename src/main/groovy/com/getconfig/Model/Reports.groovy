package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class Reports {
    @ToString(includePackage = false)
    class ReportColumn {
        String id
        String name
        String redmineField
        Map<String,String> inventories = new LinkedHashMap<>()

        ReportColumn(String id, String name, String redmineField) {
            this.id = id
            this.name = name
            this.redmineField = redmineField
        }
    }

    List<ReportColumn> columns = new ArrayList<>()

    ReportColumn addColumn(String columnId, String columnName, String redmineField) {
        ReportColumn column = new ReportColumn(columnId, columnName, redmineField)
        this.columns << column
        return column
    }
}
