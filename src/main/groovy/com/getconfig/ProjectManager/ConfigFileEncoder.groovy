package com.getconfig.ProjectManager

import com.getconfig.Config
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import com.getconfig.Model.Server
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class ConfigFileEncoder implements Controller {
    String projectHome
    String configFile
    String configFileEncrypted
    String password
    Boolean multiConfig
    ProjectUtil projectUtil = new ProjectUtil()

    void setEnvironment(ConfigEnv env) {
        this.projectHome = env.getProjectHome()
        this.configFile = env.getConfigFile()
        this.configFileEncrypted = env.getConfigFileEncripted()
        this.password = env.getPassword()
        this.multiConfig = env.getMultiConfig()
        env.accept(projectUtil)
    }

    int run() {
        if (!this.password) {
            throw new IllegalArgumentException("--password required")
        }
        if (!new File(this.configFile).exists()) {
            throw new IllegalArgumentException(
                    "config file not found : ${this.configFile}")
        }
        Config.getInstance().encrypt(this.configFile, this.password,
            multiConfig)
        return 0
    }

    int restore() {
        if (!this.password) {
            throw new IllegalArgumentException("--password required")
        }
        if (!new File(this.configFileEncrypted).exists()) {
            throw new IllegalArgumentException(
                    "encrypted config file not found : ${this.configFileEncrypted}")
        }
        Config.getInstance().decrypt(this.configFileEncrypted, this.password,
            multiConfig)
        return 0
    }
}
