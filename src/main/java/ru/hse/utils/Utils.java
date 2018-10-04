package ru.hse.utils;

import ru.hse.model.Receipt;
import ru.hse.model.User;
import ru.hse.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static Map<String, Object> createError(Errors err, Exception e) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "error");
        result.put("message", err.getMsg());
        result.put("details", e.getMessage());
        return result;
    }

    public static Map<String, Object> createError(Errors err, String token) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "error");
        result.put("token", token);
        result.put("message", err.getMsg());
        return result;
    }

    public static Map<String, Object> createError(Errors err) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "error");
        result.put("message", err.getMsg());
        return result;
    }

    public static Map<String, Object> createSuccess() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "success");
        return result;
    }

    public static Map<String, Object> createSuccess(String token, int userId, User user) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "success");
        result.put("token", token);
        result.put("userId", userId);
        result.put("userLogin", user.getLogin());
        return result;
    }

    public static Map<String, Object> createSuccess(String token, List<?> array) {
        Map<String, Object> map = createSuccess(array);
        map.put("token", token);
        return map;
    }

    public static Map<String, Object> createSuccess(Receipt receipt) {
        Map<String, Object> map = new HashMap<>();
        map.put("receipt", receipt);
        return map;
    }

    public static Map<String, Object> createSuccess(List<?> list) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("result", "success");
        map.put("info", list);
        return map;
    }

    public static Map<String, Object> createSuccess(Integer userId, List<Receipt> userReceipts) {
        Map<String, Object> result = createSuccess(userReceipts);
        result.put("userId", userId);
        return result;
    }

    public static String reformatToken(String jsonToken) {
        String[] split = jsonToken.split(":");
        String token = split[1];
        if (token.contains(" "))
            token = token.replace(" ", "");
        if (token.contains("}"))
            token = token.replace("}", "");
        if (token.charAt(0) == '"')
            token = token.substring(1);
        if (token.charAt(token.length() - 1) == '"')
            token = token.substring(0, token.length() - 1);

        return token;
    }

    public static Map<String, Object> createSuccess(UserInfo userInfo) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userInfo", userInfo);
        return result;
    }
}
