package com.getconfig.Model

import com.poiji.annotation.*

public class TestServer {
    @ExcelRow
    private int rowIndex;

    @ExcelCell(1)
    protected String serverName = "";

    @ExcelCell(2)
    protected String domain = "";

    @ExcelCell(3)
    protected String ip = "";

    @ExcelCell(4)
    protected String accountId = "";

    @ExcelCell(5)
    protected String user = "";

    @ExcelCell(6)
    protected String password = "";

    @ExcelCell(7)
    protected String remoteAlias = "";

    @ExcelCell(8)
    protected String compareServer = "";

    @ExcelCell(9)
    protected String loginOption = "";

    @Override
    public String toString() {
        return "TestServer{" + rowIndex +
                ", serverName='" + serverName + '\'' +
                ", domain='" + domain + '\'' +
                ", ip=" + ip +
                ", accountId=" + accountId +
                ", user='" + user + '\'' +
                '}';
    }

    // 属性の値チェックをし、成功したら1を返します。エラーの場合は 例外をスローします
    public int validate() {
        if (this.serverName == "" || this.domain == "") {
            return 0
        }
        // Todo : キー項目以外の値チェック
        return 1
    }
}
