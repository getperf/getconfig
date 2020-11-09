package com.getconfig.Testing

class TargetServerInfo {
    static List getParameter(TestUtil t, String name) {
        return t.platformParameters?.get(name)?.values
    }
}
