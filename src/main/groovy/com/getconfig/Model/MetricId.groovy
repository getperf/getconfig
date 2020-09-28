package com.getconfig.Model

class MetricId {

    static String make(String platform, String metric) {
        return "${platform}.${metric}"
    }

    static String platform(String id) {
        int position = id.indexOf(".")
        return (position == -1) ? id : id.substring(0, position)
    }

    static String metric(String id) {
        int position = id.indexOf(".")
        return (position == -1) ? id : id.substring(position+1, id.length())
    }

    static String orderParentKey(String platform, int orderKey) {
        return String.format("%s.%09d.%09d", platform, orderKey, 0)
    }

    static String orderChildKey(String orderParentKey, int orderChildKey) {
        String keyPrefix = orderParentKey.substring(0, orderParentKey.lastIndexOf("."))
        return String.format("%s.%09d", keyPrefix, orderChildKey)
    }
}
