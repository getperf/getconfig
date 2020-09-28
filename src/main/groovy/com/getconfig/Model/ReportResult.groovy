package com.getconfig.Model

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class ReportResult {

    // @ToString(includePackage = false)
    // class ResultSheet {
    //     String name
    //     String tracker
    //     Map <String, List<String>> platforms
    //     Set <String> servers
    // }
    List<ResultSheet> sheets = new ArrayList<>()
    Map<String, List<String>> platforms = new LinkedHashMap<>()
    Map <String, ResultSheet> platformIndex

    void makePlatformIndex() {
        platformIndex = new LinkedHashMap<>()
        sheets.each { sheet ->
            sheet.platforms.get("HW")?.each { String hwPlatform ->
                sheet.platforms.get("OS")?.each { String osPlatform ->
                    platformIndex.put("${hwPlatform}.${osPlatform}" as String, sheet)
                }
                platformIndex.put(hwPlatform, sheet)
            }
            sheet.platforms.get("OS")?.each { String osPlatform ->
                platformIndex.put(osPlatform, sheet)
            }
        }
    }

    String findPlatformCategory(String platform) {
        String platformCategory
        platforms.each { String category, List<String> platformKeys ->
            platformKeys.each {String platformKey ->
                if (platform == platformKey) {
                    platformCategory = category
                    return
                }
            }
        }
        return platformCategory
    }

    String makePlatformKey(List<String> platforms) {
        Map <String, String> categories = new LinkedHashMap<>()
        platforms.each {String platform ->
            String category = findPlatformCategory(platform)
            if (category) {
                categories.put(category, platform)
            }
        }
        String hwPlatform = categories.get("HW")
        String osPlatform = categories.get("OS")
        String paltformKey = osPlatform ?: hwPlatform 
        if ( hwPlatform && osPlatform) {
            paltformKey = "${hwPlatform}.${osPlatform}" as String
        }
        return paltformKey
    }

    ResultSheet findSheet(List<String> platforms) {
        ResultSheet reportSheet
        String platformKey = makePlatformKey(platforms)
        return platformIndex.get(platformKey)
    }
}
