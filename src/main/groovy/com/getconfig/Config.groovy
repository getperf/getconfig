package com.getconfig

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Slf4j
@CompileStatic
@Singleton
class Config {
    final String CharacterCode = "MS932"
    final String EncryptionMode = 'Blowfish'
    final String date_format = "yyyyMMdd_HHmmss"

    String configFile
    // ConfigObject config

    private byte[] decryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(EncryptionMode)
      cph.init(Cipher.DECRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    private byte[] encryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(EncryptionMode)
      cph.init(Cipher.ENCRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    private String readConfigWithEncryption(String configFile, String keyword)
        throws IOException, IllegalArgumentException {
        if (!keyword) {
            return new File(configFile).getText(CharacterCode)
        } else {
            File configFileEncrypted = new File(configFile + "-encrypted")
            if (!configFileEncrypted.exists()) {
                throw new IllegalAccessException(
                        "encrypted config file not found : ${configFileEncrypted}"
                )
            }
            SecretKeySpec key = new SecretKeySpec(keyword.getBytes(),
                EncryptionMode)
            configFileEncrypted.with {
                def buffer = decryptData(it.getBytes(), key)
                return new String(buffer, CharacterCode)
            }
        }
        // if (keyword) {
        //     configFile = configFile + "-encrypted"
        // }
        // new File(configFile).with {
        //     def decryptedFile = it.name.replaceAll(/-encrypted$/, "")
        //     if (decryptedFile != it.name || keyword == null) {
        //         return it.getText(CharacterCode)
        //     }
        //     SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)
        //     def buffer = decryptData(it.getBytes(), key)
        //     return new String(buffer, CharacterCode)
        // }
    }

    ConfigObject readConfig(String configFile = 'config/config.groovy', String keyword = null) {
        this.configFile = configFile
        String configText = this.readConfigWithEncryption(configFile, keyword)
        ConfigSlurper slurper = new ConfigSlurper()
        // this.config = slurper.parse(configText)
        return slurper.parse(configText)
    }

    void encrypt(String configFileName, String keyword = null)
        throws IOException {
        SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)
        File configFile = new File(configFileName)
        configFile.with {
            def data = encryptData(it.getBytes(), key)
            def configFileEncrypted = new File(it.parent, it.name + '-encrypted')
            configFileEncrypted.setBytes(data)
            log.info "OK\nEncrypted ${configFileEncrypted}"
        }
        configFile.delete()
    }

    void decrypt(String configFileEncrypted, String keyword = null)
        throws IOException, IllegalArgumentException {
        SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)
        new File(configFileEncrypted).with {
            def decrypteFile = it.name.replaceAll(/-encrypted$/, "")
//            log.info "${configFileEncrypted} , ${decrypteFile} ${it.name}"
            if (decrypteFile == it.name) {
                throw new IllegalArgumentException("config file name must be include '-encrypted' : ${it.name}")
            }
            def data = decryptData(it.getBytes(), key)
            def configFileDecrypted = new File(it.parent, decrypteFile)
            configFileDecrypted.setBytes(data)
            log.info "OK\nDecrypted ${configFileDecrypted}"
        }
    }
}
