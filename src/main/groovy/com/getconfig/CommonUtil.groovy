package com.getconfig

class CommonUtil {
    static boolean resetDir(String targetDir) {
        def dir = new File(targetDir)
        if (dir.exists()) {
            dir.deleteDir()
        }
        return (dir.mkdirs())
    }
}
