package com.getconfig.AgentWrapper

interface DirectExecutorWrapper {
    void setEnvironment(DirectExecutor directExecutor)
    String getLabel()
    int dryRun()
    int run()
}
