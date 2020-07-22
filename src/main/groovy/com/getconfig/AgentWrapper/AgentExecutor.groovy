package com.getconfig.AgentWrapper

import com.getconfig.ConfigEnv
import com.getconfig.Controller

interface AgentExecutor extends Controller {
    void setEnvironment(ConfigEnv env)
    int run()
    String getAgentLogDir()
}
