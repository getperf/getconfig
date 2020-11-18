package com.getconfig.Model

import com.getconfig.Document.TicketManager
import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class RedmineParameter {
    int in_operation_status_id
    Map<String,String> parameters = new LinkedHashMap<>()
    Map<String,String> custom_fields = new LinkedHashMap<>()

    String getField(String fieldName) {
        return custom_fields?.get(fieldName)
    }

    String getParameter(String parameterName) {
        return parameters?.get(parameterName)
    }

    String inventoryField() {
        return getField('inventory')
    }

    String rackLocationField() {
        return getField('rack_location')
    }

    String rackLocationFieldPrefix() {
        return getParameter('rack_location_prefix')
    }

    String portListTracker() {
        return getParameter('port_list_tracker')
    }

    String rackLocation(String subject) {
        String prefix = this.rackLocationFieldPrefix()
        return (prefix) ? prefix + subject : subject
    }

    int inOperationStatusId() {
        return in_operation_status_id
    }

    void setTicketManager(TicketManager manager) {
//        manager.inventoryField = getField('inventory')
//        manager.rackLocationField = getField('rack_location')
//        manager.rackLocationFieldPrefix = getParameter('rack_location_prefix')
//        manager.trackerPortList = getParameter('port_list_tracker')
//        manager.inOperationStatusId = in_operation_status_id
    }
}
