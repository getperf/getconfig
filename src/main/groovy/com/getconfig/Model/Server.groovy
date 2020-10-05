package com.getconfig.Model

import com.getconfig.AgentWrapper.AgentConstants
import groovy.transform.TypeChecked
import groovy.transform.CompileStatic
import groovy.transform.ToString
import com.poiji.annotation.*

@TypeChecked
@CompileStatic
@ToString(includePackage = false)
public class Server {
    // 検査レポートの行位置。低い順にサマリレポートを表示。-1 の場合は非表示
    @ExcelRow
    int order = -1;

    @ExcelCell(1)
    String serverName = "";

    @ExcelCell(2)
    String domain = "";

    @ExcelCell(3)
    String ip = "";

    @ExcelCell(4)
    String accountId = "";

    @ExcelCell(5)
    String user = "";

    @ExcelCell(6)
    String password = "";

    @ExcelCell(7)
    String remoteAlias = "";

    @ExcelCell(8)
    String compareServer = "";

    @ExcelCell(9)
    String loginOption = "";

    // キー項目の有無をチェックをします
    public boolean checkKey(String previousServerName = null) {
        if (this.serverName == "" && previousServerName) {
            this.serverName = previousServerName
        }
        return !(this.serverName == "" || this.domain == "")
    }

    // 属性の値チェックをします。エラーの場合は 例外をスローします
    public boolean validate() {
        List<String> notFoundMsgs = new ArrayList<String>()
        if (this.serverName == "" || this.domain == "") {
            notFoundMsgs << "server_name or domain"
        }
        if (domain != AgentConstants.AGENT_LABEL_LOCAL_FILE) {
            if (this.ip == "") {
                notFoundMsgs << "ip"
            }
            if (domain != AgentConstants.AGENT_LABEL_REMOTE) {
                if (this.user == "") {
                    notFoundMsgs << "user"
                }
                if (this.password == "" && this.loginOption == "") {
                    notFoundMsgs << "password or loginOption"
                }
            }
        }
        if (notFoundMsgs.size() > 0) {
            throw new IllegalArgumentException("not found value in ${this.serverName},${this.domain} : ${notFoundMsgs}")
        }
        return true
    }

    void setAccont(ConfigObject account) {
        if (this.ip == "")
            this.ip = account["server"]
        if (this.user == "")
            this.user = account["user"]
        if (this.password == "")
            this.password = account["password"]
        if (this.remoteAlias == "")
            this.remoteAlias = account["remoteAlias"]
        if (this.compareServer == "")
            this.compareServer = account["compareServer"]
        if (this.loginOption == "")
            this.loginOption = account["loginOption"]
    }
}
