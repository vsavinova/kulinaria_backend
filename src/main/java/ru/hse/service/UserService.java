package ru.hse.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.DBHelper;
import ru.hse.model.Receipt;
import ru.hse.model.User;
import ru.hse.model.UserInfo;
import ru.hse.model.Voting;
import ru.hse.utils.Errors;
import ru.hse.utils.Utils;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

// TODO: 19.03.18 Доделать поиск рецептов по ингридиентам (разобраться с массивом)

//@Service
public class UserService {
//    @Autowired
    private DBHelper dbHelper = new DBHelper();

    public User auth(String login, String hashpwd) {
        User user = null;
        try {
            ResultSet resultSet = dbHelper.requestToDB("SELECT PWD, USER_ID FROM USER_PRIVATE WHERE LOGIN " +
                    "= '" + login + "'");
            user = getUser(login, hashpwd, resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    private User getUser(String login, String password, ResultSet resultSet) throws SQLException {
        resultSet.next();
//        String pwd = resultSet.getString("PWD");
        int user_id = resultSet.getInt("USER_ID");
        String hashPwd = resultSet.getString("PWD"); //pwd.toString();
        if (password.equals(hashPwd))
            return new User(login, hashPwd, user_id);
        else
            return null;
    }

    //    rec_id->rating
    private Map<Integer, Double> getReceiptsRatingByUserId(Integer userId) throws SQLException {
        Map<Integer, Double> resultMap = new HashMap<>();
        String statement =
                "SELECT r.REC_ID, sum(v.stars)/count(v.stars) as rating FROM RECEIPT as r, VOTING as v" +
                        " WHERE r.rec_id = v.rec_id ";
        if (userId != null)
            statement += " and r.user_id = " + userId;
        statement += " GROUP BY (r.rec_id)";

        ResultSet resultSet = dbHelper.requestToDB(statement);
        while (resultSet.next()) {
            resultMap.put(resultSet.getInt("rec_id"), resultSet.getDouble("rating"));
        }
        return resultMap;
    }

    public JSONArray getUserReceipts(Integer userId) {
        JSONArray result = new JSONArray();
        try {
            Map<Integer, Double> receiptsRating = getReceiptsRatingByUserId(userId);
            ResultSet resultSet = dbHelper.requestToDB("SELECT r.REC_ID, r.REC_NAME, r.ANNOTATION, " +
                    " r.COOKING_TIME, r.PUBLICATION_DATE, r.PHOTO, " +
                    " nv.NUTR_VAL_NAME FROM RECEIPT as r, NUTRITIONAL_VALUE as nv, CATEGORY as c" +
                    " WHERE r.NUTR_ID = nv.NUTR_ID and r.USER_ID = " + userId + " GROUP BY (REC_NAME)");

            while (resultSet.next()) {
                int rec_id = resultSet.getInt("rec_id");
                String rec_name = resultSet.getString("rec_name");
//                double rating = resultSet.getDouble("rating");
                Date date = resultSet.getDate("publication_date");
                Blob photo = resultSet.getBlob("photo");
                String annotation = resultSet.getString("annotation");
                Time cooking_time = resultSet.getTime("cooking_time");
                String nutr_val_name = resultSet.getString("nutr_val_name");
                double rating = 0;
                if (receiptsRating.containsKey(rec_id))
                    rating = receiptsRating.get(rec_id);

                JSONObject receipt = new JSONObject();
                receipt.put("rec_id", rec_id);
                receipt.put("rec_name", rec_name);
                receipt.put("rating", rating);
                receipt.put("date", date);
                receipt.put("photo", photo);
                receipt.put("annotation", annotation);
                receipt.put("cooking_time", cooking_time);
                receipt.put("nutr_val_name", nutr_val_name);
                receipt.put("cooking_time", cooking_time);
                result.put(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String addFavoriteBToUser(String token, Integer userId, Integer recId) {
        String result = Utils.createSuccess();
        token = Utils.getToken(token);
        boolean checkToken = TockenManager.checkToken(token, userId);
        if (checkToken)
            try {
                ResultSet resultSet = dbHelper.requestToDB(
                        "INSERT INTO FAVORITE (user_id, rec_id) VALUES ("
                                + userId + ", " + recId + ")");
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(), e.getMessage());
                e.printStackTrace();
            }
        else
            result = Utils.createError(Errors.INVALID_TOKEN.getMsg(), "");
            return result;
    }

    public JSONArray getFavoriteByUserId(Integer userId) {
        JSONArray result = new JSONArray();
        try {
            Map<Integer, Double> receiptsRating = getReceiptsRatingByUserId(null);
            ResultSet resultSet = dbHelper.requestToDB(
                    "SELECT r.REC_ID, r.REC_NAME, u.USER_NAME, r.PUBLICATION_DATE, r.PHOTO, r.ANNOTATION, " +
                            " r.COOKING_TIME, nv.NUTR_VAL_NAME FROM FAVORITE as f, RECEIPT as r, USER u, " +
                            " NUTRITIONAL_VALUE as nv, CATEGORY as c WHERE f.rec_id = r.REC_ID " +
                            " and f.user_id = " + userId + " AND r.USER_ID = u.USER_ID AND r.NUTR_ID = nv.NUTR_ID " +
                            " AND r.CAT_ID = c.CAT_ID");

            while (resultSet.next()) {
                int rec_id = resultSet.getInt("rec_id");
                String rec_name = resultSet.getString("rec_name");
                String user_name = resultSet.getString("user_name");
                Date date = resultSet.getDate("publication_date");
                Blob photo = resultSet.getBlob("photo");
                String annotation = resultSet.getString("annotation");
                Time cooking_time = resultSet.getTime("cooking_time");
                String nutr_val_name = resultSet.getString("nutr_val_name");
                double rating = 0;
                if (receiptsRating.containsKey(rec_id))
                    rating = receiptsRating.get(rec_id);

                JSONObject receipt = new JSONObject();
                receipt.put("rec_id", rec_id);
                receipt.put("rec_name", rec_name);
                receipt.put("user_name", user_name);
                receipt.put("rating", rating);
                receipt.put("date", date);
                receipt.put("photo", photo);
                receipt.put("annotation", annotation);
                receipt.put("cooking_time", cooking_time);
                receipt.put("nutr_val_name", nutr_val_name);
                receipt.put("cooking_time", cooking_time);
                result.put(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public String register(String login, String pwd) {
        String result = "";
        try {
            ResultSet resultSet = dbHelper.requestToDBAndGetGenerated(
                    String.format("INSERT INTO USER_PRIVATE (LOGIN, PWD, REGISTRATION_DATE) " +
                    "VALUES ('%s', '%s'" + new Date(System.currentTimeMillis()) + ")", login, pwd));
            if (resultSet.next()) {
                String token = TockenManager.getToken(new User(login, pwd,
                        resultSet.getInt(1)));
                 result = Utils.createSuccess(token);
            }
        } catch (SQLException e) {
            result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(), e.getMessage());
            e.printStackTrace();
        } catch (Exception e){
            result = Utils.createError(Errors.UNKNOWN_ERROR.getMsg(), e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public DBHelper getHelper() {
        return dbHelper;
    }

    public String addUserInfo(UserInfo userInfo) {
        String result;
        boolean checkToken = TockenManager.checkToken(userInfo.getToken(), userInfo.getUserId());
        if (checkToken) {
            try {
                ResultSet resultSet = dbHelper.requestToDB(String.format("INSERT INTO USER(USER_ID, USER_NAME, PHOTO, " +
                                "REGISTRATION_DATE, BIO) VALUES (%d, '%s', " + userInfo.getPhoto() +
                                ", '%s', '%s')", userInfo.getUserId(), userInfo.getUserName(),
                        new Date(System.currentTimeMillis()), userInfo.getBio()));
                result = Utils.createSuccess();
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(), e.getMessage());
                e.printStackTrace();
            }
        } else
            result = Utils.createError(Errors.INVALID_TOKEN.getMsg(), "");
        return result;
    }

    public String addMark(Voting voting) {
        String result;
        boolean checkToken = TockenManager.checkToken(voting.getToken(), voting.getUserId());
        if (checkToken) {
            try {
                ResultSet resultSet = dbHelper.requestToDB(
                        String.format("INSERT INTO VOTING (user_id, rec_id, stars, VOTING_DATE) " +
                                "VALUES (%d, %d, %d, '" + new Date(System.currentTimeMillis()) + "')",
                                voting.getUserId(), voting.getRecId(), voting.getStars()));
                result = Utils.createSuccess();
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(), e.getMessage());
                e.printStackTrace();
            }
        } else
            result = Utils.createError(Errors.INVALID_TOKEN.getMsg(), "");
        return result;
    }

    public String createReceipt(Receipt receipt) {
        String result;
        boolean checkToken = TockenManager.checkToken(receipt.getToken(), receipt.getUserId());
        if (checkToken) {
            try {
                result = dbHelper.createReceipt(receipt);
                if (result.equals("error"))
                    result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(),
                            "Ошибка генерации id");
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR.getMsg(), e.getMessage());
                e.printStackTrace();
            }
        } else
            result = Utils.createError(Errors.INVALID_TOKEN.getMsg(), "");
        return result;
    }

    public String updateInfo(UserInfo user) {
        return "";
    }
}
