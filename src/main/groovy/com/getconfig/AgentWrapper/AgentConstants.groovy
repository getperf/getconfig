package com.getconfig.AgentWrapper

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class AgentConstants {
    // エージェントコマンド実行時のログレベル
    // None 0, FATAL 1, CRIT 2, ERR 3, WARN 4, NOTICE 5, INFO 6, DBG 7
    public static final int AGENT_LOG_LEVEL = 3

    // リモートエージェントラベル
    public static final String AGENT_LABEL_REMOTE = "{Agent}"

    // リモートエージェントURLプレフィックス
    public static final String REMOTE_AGENT_URL_PREFIX = "https://"

    // リモートエージェント接続ポート
    public static final int REMOTE_AGENT_PORT = 59443

    // HUBサーバローカルファイル用エージェントラベル
    public static final String AGENT_LABEL_LOCAL_FILE = "{LocalFile}"

}
