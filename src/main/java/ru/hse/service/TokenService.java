package ru.hse.service;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;
import ru.hse.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public interface TokenService {
    String getToken(User user);

    boolean checkToken(String token, int userId);

    boolean checkToken(String token);

    @Service
    class Impl implements TokenService {
        private static final String key = "secret";

        public String getToken(User user) {
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("clientType", "user");
            tokenData.put("userId", user.getUserId());
            tokenData.put("login", user.getLogin());
            tokenData.put("token_create_date", new Date().getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 100);
            tokenData.put("token_expiration_date", calendar.getTime());
            JwtBuilder jwtBuilder = Jwts.builder();
            jwtBuilder.setExpiration(calendar.getTime());
            jwtBuilder.setClaims(tokenData);

            String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, key).compact();
            return token;
        }

        public boolean checkToken(String token, int userId) {
            boolean result = true;
            JwtParser jwtParser = Jwts.parser().setSigningKey(key);
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            Claims body = claimsJws.getBody();
            String login = body.get("login", String.class);
            Integer id = body.get("userId", Integer.class);
            Long token_create_date = body.get("token_create_date", Long.class);
            Long token_expiration_date = body.get("token_expiration_date", Long.class);
            if (!id.equals(userId) || token_expiration_date < new Date().getTime())
                result = false;
            return result;
        }


        public boolean checkToken(String token) {
            boolean result = true;
            JwtParser jwtParser = Jwts.parser().setSigningKey(key);
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            Claims body = claimsJws.getBody();
            Long token_expiration_date = body.get("token_expiration_date", Long.class);
            if (token_expiration_date < new Date().getTime())
                result = false;
            return result;
        }

        private String createHash(String pwd) {
            String hash = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(pwd.getBytes());
                byte byteData[] = md.digest();

                //convert the byte to hex format method 2
                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < byteData.length; i++) {
                    String hex = Integer.toHexString(0xff & byteData[i]);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                hash = hexString.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return hash;
        }

    }
}
