package ru.hse.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UserService {
    User auth(String login, String hashpwd);

    List<Receipt> getUserReceipts(Integer userId);

    Map<String, Object> addFavoriteBToUser(Integer userId, Integer recId);

    List<Receipt> getFavoriteByUserId(Integer userId);

    Map<String, Object> register(String token, String login, String pwd);

    Map<String, Object> addUserInfo(UserInfo userInfo);

    Map<String, Object> addMark(Voting voting);

    Map<String, Object> createReceipt(Receipt receipt);

    Map<String, Object> getUserInfo(Integer userId);

    @Service
    class Impl implements UserService {
        @Autowired
        private DBHelper dbHelper;

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
            User user;
            resultSet.next();
//        String pwd = resultSet.getString("PWD");
            int user_id = resultSet.getInt("USER_ID");
            String hashPwd = resultSet.getString("PWD"); //pwd.toString();
            if (password.equals(hashPwd)) {
                user = new User(login, hashPwd);
                user.setUserId(user_id);
            } else
                user = null;
            return user;
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

        public List<Receipt> getUserReceipts(Integer userId) {
            List<Receipt> receipts = new ArrayList<>();
            try {
                Map<Integer, Double> receiptsRating = getReceiptsRatingByUserId(userId);
                ResultSet resultSet = dbHelper.requestToDB("SELECT r.REC_ID, r.REC_NAME, r.ANNOTATION, " +
                        " r.TIME, r.PUBLICATION_DATE, r.PHOTO, c.CAT_NAME, " +
                        " nv.NUTR_VAL_NAME FROM RECEIPT as r, NUTRITIONAL_VALUE as nv, CATEGORY as c" +
                        " WHERE r.NUTR_ID = nv.NUTR_ID and c.CAT_ID =r.CAT_ID and r.USER_ID = " + userId + " GROUP BY (REC_NAME)");

                while (resultSet.next()) {
                    int rec_id = resultSet.getInt("rec_id");
                    String rec_name = resultSet.getString("rec_name");
//                double rating = resultSet.getDouble("rating");
                    Date date = resultSet.getDate("publication_date");
                    String photo = resultSet.getString("photo");
                    String annotation = resultSet.getString("annotation");
                    Integer cooking_time = resultSet.getInt("time");
                    String nutr_val_name = resultSet.getString("nutr_val_name");
                    String cat_name = resultSet.getString("cat_name");
                    double rating = 0;
                    if (receiptsRating.containsKey(rec_id))
                        rating = receiptsRating.get(rec_id);

                    Receipt receipt = new Receipt();
                    receipt.setRecId(rec_id);
                    receipt.setRecName(rec_name);
                    receipt.setPhoto(photo);
                    receipt.setTime(cooking_time);
                    receipt.setRating(rating);
                    receipt.setPublicationDate(date);
                    receipt.setAnnotation(annotation);
                    receipt.setDifficulty(nutr_val_name);
                    receipt.setCategory(cat_name);
                    receipts.add(receipt);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return receipts;
        }

        public Map<String, Object> addFavoriteBToUser(Integer userId, Integer recId) {
            Map<String, Object> result = Utils.createSuccess();
            try {
                ResultSet resultSet = dbHelper.requestToDB(
                        "INSERT INTO FAVORITE (user_id, rec_id) VALUES ("
                                + userId + ", " + recId + ")");
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        public List<Receipt> getFavoriteByUserId(Integer userId) {
            ArrayList<Receipt> receipts = new ArrayList<>();
            try {
                Map<Integer, Double> receiptsRating = getReceiptsRatingByUserId(null);
                ResultSet resultSet = dbHelper.requestToDB(
                        "SELECT r.REC_ID, r.REC_NAME, u.USER_NAME, r.PUBLICATION_DATE, r.PHOTO, r.ANNOTATION, " +
                                " r.TIME, nv.NUTR_VAL_NAME FROM FAVORITE as f, RECEIPT as r, USER u, " +
                                " NUTRITIONAL_VALUE as nv, CATEGORY as c WHERE f.rec_id = r.REC_ID " +
                                " and f.user_id = " + userId + " AND r.USER_ID = u.USER_ID AND r.NUTR_ID = nv.NUTR_ID " +
                                " AND r.CAT_ID = c.CAT_ID");

                while (resultSet.next()) {
                    int rec_id = resultSet.getInt("rec_id");
                    String rec_name = resultSet.getString("rec_name");
                    String user_name = resultSet.getString("user_name");
                    Date date = resultSet.getDate("publication_date");
                    String photo = resultSet.getString("photo");
                    String annotation = resultSet.getString("annotation");
                    Integer cooking_time = resultSet.getInt("time");
                    String nutr_val_name = resultSet.getString("nutr_val_name");
                    double rating = 0;
                    if (receiptsRating.containsKey(rec_id))
                        rating = receiptsRating.get(rec_id);

                    Receipt receipt = new Receipt();
                    receipt.setRecId(rec_id);
                    receipt.setRecName(rec_name);
                    receipt.setRating(rating);
                    receipt.setPublicationDate(date);
                    receipt.setPhoto(photo);
                    receipt.setAnnotation(annotation);
                    receipt.setTime(cooking_time);
                    receipt.setCategory(nutr_val_name);
                    receipts.add(receipt);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return receipts;
        }


        public Map<String, Object> register(String token, String login, String pwd) {
            Map<String, Object> result = new HashMap<>();
            try {
                ResultSet resultSet = dbHelper.requestToDBAndGetGenerated(
                        String.format("INSERT INTO USER_PRIVATE (LOGIN, PWD, REGISTRATION_DATE) " +
                                "VALUES ('%s', '%s'" + new Date(System.currentTimeMillis()) + ")", login, pwd));
                if (resultSet.next()) {
                    User user = new User(login, pwd);
                    int userId = resultSet.getInt(1);
                    user.setUserId(userId);
                    result = Utils.createSuccess(token, userId, user);
                }
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR, e);
                e.printStackTrace();
            } catch (Exception e) {
                result = Utils.createError(Errors.UNKNOWN_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        public Map<String, Object> addUserInfo(UserInfo userInfo) {
            Map<String, Object> result;
            try {
                ResultSet resultSet = dbHelper.requestToDB(String.format("INSERT INTO USER(USER_ID, USER_NAME, PHOTO, " +
                                "REGISTRATION_DATE, BIO) VALUES (%d, '%s', " + userInfo.getPhoto() +
                                ", '%s', '%s')", userInfo.getUserId(), userInfo.getUserName(),
                        new Date(System.currentTimeMillis()), userInfo.getBio()));
                result = Utils.createSuccess();
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        public Map<String, Object> addMark(Voting voting) {
            Map<String, Object> result;
            try {
                ResultSet resultSet = dbHelper.requestToDB(
                        String.format("INSERT INTO VOTING (user_id, rec_id, stars, VOTING_DATE) " +
                                        "VALUES (%d, %d, %d, '" + new Date(System.currentTimeMillis()) + "')",
                                voting.getUserId(), voting.getRecId(), voting.getStars()));
                result = Utils.createSuccess();
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        public Map<String, Object> createReceipt(Receipt receipt) {
            Map<String, Object> result = new HashMap<>();
            try {
                int updateResult = dbHelper.createReceipt(receipt);
                if (updateResult == -1)
                    result = Utils.createError(Errors.SQL_INSERT_ERROR);
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_INSERT_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        public Map<String, Object> getUserInfo(Integer userId) {
            UserInfo userInfo = dbHelper.getUserInfo(userId);
            return Utils.createSuccess(userInfo);
        }
    }
}