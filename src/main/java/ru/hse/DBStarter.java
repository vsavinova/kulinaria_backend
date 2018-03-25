package ru.hse;

//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTCreationException;
import io.jsonwebtoken.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DBStarter {

    static  String key = "abc123";
//    static String testRSA() {
//        String token = "";
//        try {
//            Algorithm algorithm = Algorithm.HMAC256("secret");
//            token = JWT.create()
//                    .sign(algorithm);
//        } catch (JWTCreationException exception) {
//            //Invalid Signing configuration / Couldn't convert Claims.
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return token;
//    }
//
//    static String testToken() {
//        String token = "";
//        try {
//            Algorithm algorithm = Algorithm.HMAC256("secret");
//            token = JWT.create()
//                    .withIssuer("auth0")
//                    .withIssuedAt(new Date(1516239022))
//                    .withSubject("1234567890")
//                    .sign(algorithm);
//        } catch (UnsupportedEncodingException exception) {
//            //UTF-8 encoding not supported
//        } catch (JWTCreationException exception) {
//            //Invalid Signing configuration / Couldn't convert Claims.
//        }
//        return token;
//
//    }

    private static String getHash(String pwd) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(pwd.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<byteData.length;i++) {
            String hex=Integer.toHexString(0xff & byteData[i]);
            if(hex.length()==1) hexString.append('0');
            hexString.append(hex);
        }
        String hash = hexString.toString();
        System.out.println("Pwd(in hex format):: " + hash);
        return hash;
    }

    public static void main(String[] args) {
        try {
            String qwerty123 = getHash("qwerty123");
//            String token = testRSA(); //testToken();
//            Class.forName("org.h2.Driver");
//            Connection conn = DriverManager.
//                    getConnection("jdbc:h2:~/food", "admin", "admin");
//            conn.close();
            String compact = Jwts.builder()
                    .setSubject("Joe")
                    .signWith(SignatureAlgorithm.HS512, key)
                    .compact();
            auth(compact);
            System.out.println("Hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getToken(Integer usetId, String userName){
        Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("clientType", "user");
            tokenData.put("userID", usetId.toString());
            tokenData.put("username", userName);
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

    static void auth(String token){
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
        String subject = claimsJws.getBody().getSubject();
        String signature = claimsJws.getSignature();
        System.out.println(claimsJws.getHeader());
        System.out.println(subject);
        System.out.println(signature);

    }
}
