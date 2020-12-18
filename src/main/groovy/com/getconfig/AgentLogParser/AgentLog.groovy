package com.getconfig.AgentLogParser

import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.nio.file.Paths

@CompileStatic
@ToString
class AgentLog {
    String path
    AgentLogMode agentLogMode
    String serverName
    String alias
    String platform
    String metricFile
    String base

    AgentLog(String path, String base = null) {
        this.path = path
        this.base = base
        this.agentLogMode = AgentLogMode.UNKNOWN
    }

    AgentLog(AgentLog parent) {
        this.path = parent.path
        this.agentLogMode = parent.agentLogMode
        this.serverName = parent.serverName
        this.alias = parent.alias
        this.platform = parent.platform
        this.metricFile = parent.metricFile
        this.base = parent.base
    }

    AgentLog parse() {
        String[] paths = new File(this.path).toPath().collect()*.toString()
        this.agentLogMode = AgentLogMode.UNKNOWN
        this.metricFile = (paths.length>0)?paths[(paths.length-1)]:null;
        // if (!this.metricFile || paths.length <= 2) {
        //     return this
        // }
        if (!this.metricFile) {
            return this
        }

        // ローカルエージェントバッチログの解析
        // 例：\LocalAgentBatch_vCenter_192.168.10.100_Account01\centos80\all.json
        if (paths[0].startsWith("LocalAgentBatch")) {
            // if (paths.length < 3) {
            //     return this
            // }
            // this.platform = paths[0].tokenize('_')[1]
            // this.alias = paths[1]
            // this.agentLogMode = AgentLogMode.BATCH

            this.platform = paths[0].tokenize('_')[1]
            if (!platform) {
                 return this
            }
            this.agentLogMode = AgentLogMode.BATCH
            if (paths.length >= 3) {
                this.alias = paths[1]
            }

        // ローカルエージェントバッチ以外で2階層以下のログは除外する
        } else if (paths.length <= 2) {
            return this

        // リモートエージェントログの解析
        // 例：\server01\LinuxConf\20200722\103500\centos80\cpu
        } else if (paths[2]=~/^\d+$/ && paths[3]=~/^\d+$/) {
            if (paths[1].endsWith("Conf")) {
                if (paths.length < 6) {
                    return this
                }
                String platform = paths[1]
                this.serverName = paths[0]
                this.alias = paths[4]
                this.platform = platform.substring(0, platform.length() - 4)
                this.agentLogMode = AgentLogMode.NORMAL
            }
        // ハブサーバログの解析
        } else if (paths[1].endsWith("Conf")) {
            if (paths.length < 3) {
                return this
            }
            String platform = paths[1]
            this.serverName = paths[0]
            this.platform = platform.substring(0, platform.length() - 4)
            this.agentLogMode = AgentLogMode.NORMAL

        // ローカルエージェントログパスの解析
        } else if (paths.length >= 4) {
            this.serverName = paths[0]
            this.platform = paths[1]
            this.alias = paths[2]
            this.agentLogMode = AgentLogMode.NORMAL
        }

        return this
    }

    void patchServerName(ServerNameAliases serverNameAliases) {
        // println serverNameAliases
        if (!this.serverName && this.agentLogMode == AgentLogMode.BATCH) {
            if (this.alias) {
                this.serverName = serverNameAliases.getServerName(this.alias)
            }
        }
    }

    String getProjectLogPath() {
        if (!serverName || !platform || !metricFile) {
            return null
        }
        return Paths.get(this.serverName, this.platform, this.serverName, this.metricFile)
    }

    String getLogPath() {
        return Paths.get(base, path)
    }
}
