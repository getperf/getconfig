package com.getconfig

class CommonUtil {
    static boolean resetDir(String targetDir) {
        def dir = new File(targetDir)
        if (dir.exists()) {
            dir.deleteDir()
        }
        return (dir.mkdirs())
    }

    static String toCamelCase( String text, boolean capitalized = false ) {
        text = text.replaceAll( "([ _\\.\\-])([A-Za-z0-9])", {
            List<String> it -> it[2].toUpperCase() } )
        return capitalized ? text.capitalize() : text
    }
}
