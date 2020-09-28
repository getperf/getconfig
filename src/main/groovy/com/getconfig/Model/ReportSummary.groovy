package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ReportSummary {
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

        String findMetricId(List<String> platforms) {
            String metricId
            platforms.each { String platform ->
                if (inventories?.containsKey(platform)) {
                    metricId = MetricId.make(platform, inventories.get(platform))
                }
            }
            return metricId
        }
    }

    List<ReportColumn> columns = new ArrayList<>()
    Set <String> servers = new LinkedHashSet<>()

    ReportColumn addColumn(String columnId, String columnName, String redmineField) {
        ReportColumn column = new ReportColumn(columnId, columnName, redmineField)
        this.columns << column
        return column
    }

    Map<String, ReportColumn> getColumns() {
        Map<String, ReportColumn> reportColumns = new LinkedHashMap<>()
        this.columns.each { column ->
            reportColumns.put(column.id, column)
        }
        return reportColumns
    }
}
