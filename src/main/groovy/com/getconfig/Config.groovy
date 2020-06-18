package com.getconfig

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import groovy.transform.ToString

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
    ConfigObject config

    byte[] decryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(EncryptionMode)
      cph.init(Cipher.DECRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    byte[] encryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(EncryptionMode)
      cph.init(Cipher.ENCRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    String readConfigWithEncription(String configFile, String keyword)
        throws IOException, IllegalArgumentException {
        new File(configFile).with {
            def decrypte_file_name = it.name.replaceAll(/-encrypted$/, "")
            if (decrypte_file_name != it.name || keyword == null) {
                return it.getText(CharacterCode)
            }
            SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)
            def buffer = decryptData(it.getBytes(), key)
            return new String(buffer, CharacterCode)
        }
    }

    void readConfig(String configFile = 'config/config.groovy', String keyword = null) {
        this.configFile = configFile
        String configText = this.readConfigWithEncription(configFile, keyword)
        ConfigSlurper slurper = new ConfigSlurper()
        this.config = slurper.parse(configText)
    }

    void encrypt(String configFile, String keyword = null)
        throws IOException {
        SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)

        new File(configFile).with {
            def data = encryptData(it.getBytes(), key)
            def configFileEncrypted = new File(it.parent, it.name + '-encrypted')
            configFileEncrypted.setBytes(data)
            log.info "OK\nEncrypted ${configFileEncrypted}"
        }
    }

    void decrypt(String configFileEncrypted, String keyword = null)
        throws IOException, IllegalArgumentException {
        SecretKeySpec key = new SecretKeySpec(keyword.getBytes(), EncryptionMode)
        new File(configFileEncrypted).with {
            def decrypteFile = it.name.replaceAll(/-encrypted$/, "")
            log.info "${configFileEncrypted} , ${decrypteFile} ${it.name}"
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
