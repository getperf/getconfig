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

    @ExcelCell(0)
    String serverName = "";

    @ExcelCell(1)
    String domain = "";

    @ExcelCell(2)
    String ip = "";

    @ExcelCell(3)
    String accountId = "";

    @ExcelCell(4)
    String user = "";

    @ExcelCell(5)
    String password = "";

    @ExcelCell(6)
    String remoteAlias = "";

    @ExcelCell(7)
    String compareServer = "";

    @ExcelCell(8)
    String loginOption = "";

    boolean dryRun = false;
    boolean indirectConnection = false;
    String domainExt = "";

    // キー項目の有無をチェックをします
    public boolean checkKey(String previousServerName = null, 
                            String previousCompareServer = null) {
        if (this.serverName == "" && previousServerName) {
            this.serverName = previousServerName
        }
        if (this.compareServer == "" && previousCompareServer) {
            this.compareServer = previousCompareServer
        }
        return !(this.serverName == "" || this.domain == "")
    }

    // 属性の値チェックをします。エラーの場合は 例外をスローします
    public boolean validate() {
        List<String> notFoundMsgs = new ArrayList<String>()
        if (this.serverName == "" || this.domain == "") {
            notFoundMsgs << "server_name or domain"
        }

        if (this.dryRun) {
            return true
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

    String serverDomainKey() {
        return "${this.serverName}.${this.domain}"
    }

    void setAccount(ConfigObject account) {
        if (this.ip == "") {
            this.indirectConnection = true
            this.ip = account["server"]
        }
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

    // 比較対象サーバが検査対象に含まれない場合、新規サーバを生成する。
    // DryRun モードでの実行のみを想定し、コレクターが実行出来るよう、dryRun を true にする
    Server cloneComparedServer(String serverName) {
        Server addedServer = new Server()
        addedServer.serverName = serverName
        addedServer.domain = this.domain
        addedServer.dryRun = true

        return addedServer
    }

    void initDomain() {
        (this.domain=~/(.+)\.(.+)/).each { String m0, m1, m2 ->
            this.domain = m1
            this.domainExt = m2
        }
    }
}
