package com.marketplace.gateway.parser;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RSAKeyParser {

  public RSAPublicKey loadPublic(Resource resource) throws Exception {
    String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    key = key.replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s+", "");

    byte[] decoded = Base64.getDecoder().decode(key);
    return (RSAPublicKey) KeyFactory.getInstance("RSA")
        .generatePublic(new X509EncodedKeySpec(decoded));
  }
}
