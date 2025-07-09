package com.woozuda.backend.note.entity.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AesEncryptor {

    public static final String SALT_DATA_DELIMITER = "::";

    private final String password;

    public AesEncryptor(@Value("${aes.password}") String password) {
        this.password = password;
    }

    public String encrypt(String plainText) {
        //무작위 salt 생성
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.delux(password, salt);

        // salt를 암호화된 데이터와 함께 저장
        return salt + SALT_DATA_DELIMITER + encryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText) {
        // 암호화된 데이터에서 salt 분리
        String[] parts = encryptedText.split(SALT_DATA_DELIMITER, 2);
        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }
        if (parts.length == 1) {
            return encryptedText;
        }

        String salt = parts[0];
        String cipherText = parts[1];

        TextEncryptor encryptor = Encryptors.delux(password, salt);

        return encryptor.decrypt(cipherText);
    }


}
