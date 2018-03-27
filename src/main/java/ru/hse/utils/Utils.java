package ru.hse.utils;

import org.json.JSONObject;

public class Utils {
    public static String createError(String msg, String details) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "error");
        jsonObject.put("message", msg);
        jsonObject.put("details", details);
        return jsonObject.toString();
    }

    public static String createSuccess() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "success");
        return jsonObject.toString();
    }

    public static String createSuccess(String token) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "success");
        jsonObject.put("token", token);
        return jsonObject.toString();
    }

    public static String createSuccess(String token, int userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "success");
        jsonObject.put("token", token);
        jsonObject.put("userId", userId);
        return jsonObject.toString();
    }

    public static String getToken(String jsonToken) {
        String[] split = jsonToken.split(":");
        String token = split[1];
        if (token.contains(" "))
            token = token.replace(" ", "");
        if (token.contains("}"))
            token = token.replace("}", "");
        if (token.charAt(0) == '"')
            token = token.substring(1);
        if (token.charAt(token.length()-1) == '"')
            token = token.substring(0, token.length()-1);

        return token;
    }
}
