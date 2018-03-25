package ru.hse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ru.hse.model.Ingredient;
import ru.hse.model.Receipt;
import ru.hse.model.Step;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Component
public class DBHelper {

    Connection connection;

    public DBHelper() {
        createConnection();
    }

    private void createConnection() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.
                    getConnection("jdbc:h2:~/food", "admin", "admin");
            System.out.println("Hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray getAllReceipts() {
        JSONArray result = new JSONArray();
        try {
            ResultSet resultSet = requestToDB("SELECT r.REC_NAME, r.PHOTO, " +
                    " r.TIME, sum(v.stars)/count(v.stars) as rating" +
                    ", nv.NUTR_VAL_NAME, cat.CAT_NAME" +
                    " from RECEIPT as r, VOTING as v, NUTRITIONAL_VALUE as nv, CATEGORY as cat" +
                    " WHERE r.rec_id = v.rec_id " +
                    " and r.NUTR_ID=nv.NUTR_ID and r.CAT_ID = cat.CAT_ID " +
                    " GROUP BY (REC_NAME) " +
                    " UNION SELECT r.REC_NAME, r.PHOTO, r.TIME, 0 as rating, nv.NUTR_VAL_NAME, " +
                    " cat.CAT_NAME from RECEIPT as r, VOTING as v, NUTRITIONAL_VALUE as nv, CATEGORY as cat" +
                    " WHERE r.REC_ID NOT IN (SELECT v.rec_id FROM VOTING as v)" +
                    " and r.NUTR_ID=nv.NUTR_ID and r.CAT_ID = cat.CAT_ID");
            while (resultSet.next()) {
                String recName = resultSet.getString("REC_NAME");
                Blob photo = resultSet.getBlob("PHOTO");
                Integer cooking_time = resultSet.getInt("TIME");
                double rating = resultSet.getDouble("RATING");
                String nutr_val_name = resultSet.getString("NUTR_VAL_NAME");
                String category = resultSet.getString("CAT_NAME");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rec_name", recName);
                jsonObject.put("photo", photo);
                jsonObject.put("time", cooking_time);
                jsonObject.put("rating", rating);
                jsonObject.put("dificulty", nutr_val_name);
                jsonObject.put("category", category);
                result.put(jsonObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int getReceptsCount(Integer userId) throws SQLException {
        ResultSet resultSet = requestToDB("SELECT count(r.REC_ID) as receipts FROM RECEIPT " +
                "as r where r.USER_ID = " + userId);
        if (resultSet.next())
            return resultSet.getInt("receipts");
        else
            return 0;
    }

    public JSONObject getUserInfo(Integer userId) {
        JSONObject result = new JSONObject();
        try {
            ResultSet resultSet = requestToDB(String.format("SELECT u.USER_NAME, u.PHOTO, u.BIO, " +
                            "u.REGISTRATION_DATE as rd from USER as u where u.USER_ID = %d",
                    userId, userId));
            if (resultSet.next()) {
                String name = resultSet.getString("user_name");
                Blob userPhoto = resultSet.getBlob("photo");
                String story = resultSet.getString("bio");
                Date date = resultSet.getDate("rd");
                result.put("user_name", name);
                result.put("user_photo", userPhoto);
                result.put("bio", story);
                result.put("rd", date);
            }
            int receipts = getReceptsCount(userId);
            result.put("receipts", receipts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //    Получение рейтинга рецепта по id рецепта
    private double getRating(int rec_id) {
        double rating = 0;
        String statement = "Select * from VOTING as v WHERE v.REC_ID = " + rec_id +
                " ORDER BY v.VOTING_DATE ASC";
        try {
            ResultSet resultSet = requestToDB(statement);
            HashMap<Integer, Integer> votings = new HashMap<>();
            double sum = 0;
            while (resultSet.next()) {
                int user_id = resultSet.getInt("user_id");
                int stars = resultSet.getInt("stars");
                if (votings.containsKey(user_id))
                    sum -= votings.get(user_id);
                votings.put(user_id, stars);
                sum += stars;
            }
            int votingCount = votings.size();
            if (votingCount != 0)
                rating = sum / votingCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rating;
    }


    private ArrayList<Integer> getReceipitIdswithIngr(Integer ingr_id) throws SQLException {
        ArrayList<Integer> rec_ids = new ArrayList<>();
        String statement =
                "SELECT rd.REC_ID FROM RECEIPT_DETAILS as rd WHERE rd.INGR_ID = 1";
        ResultSet resultSet = requestToDB(statement);
        while (resultSet.next()) {
            rec_ids.add(resultSet.getInt("rec_id"));
        }
        return rec_ids;
    }

    public JSONArray findReceipt(Integer ingrId, Integer cookTime, Integer nutrVal, String recName) {
        JSONArray result = new JSONArray();
        try {
            String statement =
                    "Select r.REC_ID, r.REC_NAME, r.PHOTO, r.ANNOTATION, nv.NUTR_VAL_NAME, " +
                            " nv.NUTR_ID, r.COOKING_TIME, c.CAT_NAME from RECEIPT as r, CATEGORY as c, " +
                            "NUTRITIONAL_VALUE as nv WHERE  r.NUTR_ID = nv.NUTR_ID AND c.CAT_ID = r.CAT_ID ";
            ArrayList<Integer> recIdsWithIngr = new ArrayList<>();
            if (ingrId != null) {
                recIdsWithIngr = getReceipitIdswithIngr(ingrId);
            }
            if (cookTime != null)
                statement += " AND r.COOKING_TIME  <= \'" + cookTime + "\'";
            if (nutrVal != null)
                statement += " AND nv.NUTR_ID = " + nutrVal;
            if (recName != null)
                statement += " AND r.REC_NAME LIKE '\'%" + recName + "%\'";
            statement += "GROUP BY (r.REC_ID, nv.NUTR_ID)";

            ResultSet resultSet = requestToDB(statement);
            boolean isFirstRow = true;

            while (resultSet.next()) {
                int rec_id = resultSet.getInt("rec_id");
                double rating = getRating(rec_id); // TODO: 16.03.18 Переделать, чтобы в запросе получать рейтинг
                String rec_name = resultSet.getString("rec_name");
                Blob photo = resultSet.getBlob("receipt.photo");
                String annotation = resultSet.getString("annotation");
                Time cooking_time = resultSet.getTime("cooking_time");
                String nutr_val_name = resultSet.getString("nutr_val_name");

                JSONObject receipt = new JSONObject();
                receipt.put("rec_id", rec_id);
                receipt.put("rec_name", rec_name);
                receipt.put("rating", rating);
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

    public String createReceipt(Receipt receipt) throws SQLException {
        String photo = receipt.getPhoto();
        java.util.Date cooking_time = receipt.getTime();
        String statement1 = String.format("INSERT INTO RECEIPT (REC_NAME, USER_ID, PUBLICATION_DATE," +
                "                     PHOTO, ANNOTATION, COOKING_TIME, NUTR_ID, CAT_ID)" +
                "VALUES ('%s', %d, '" + new Date(System.currentTimeMillis()) + "', " +
                        (photo == null ? null : "'" + photo + "'") +  ", '%s', "
                + (cooking_time == null ? null : "'" + cooking_time + "'") + ", %d, %d)",
                receipt.getRecName(), receipt.getUserId(),
                receipt.getAnnotation(), receipt.getNutrId(), receipt.getCatId());
        PreparedStatement preparedStatement = connection.prepareStatement(statement1);
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            int recId = resultSet.getInt(1);
            for (Ingredient ingredient : receipt.getIngredients()) {
                String statement2 = String.format(Locale.US, "INSERT INTO RECEIPT_DETAILS (REC_ID, INGR_ID, " +
                        "COUNT, UNIT) VALUES (%d, %d, %f, '%s')", recId, ingredient.getIngrId(),
                        ingredient.getCount(), ingredient.getUnit());
                preparedStatement = connection.prepareStatement(statement2);
                preparedStatement.execute();
            }
            for (Step step : receipt.getSteps()) {
                String statement3 = String.format("INSERT INTO STEPS (PHOTO, DESCRIPTION, REC_ID) " +
                        "VALUES ('%s', '%s', %d)", step.getPhoto(), step.getDescription(),
                        recId);
                preparedStatement = connection.prepareStatement(statement3);
                preparedStatement.execute();
            }
            connection.commit();
        } else
            return "error";
        return "success";
    }

    public ResultSet requestToDB(String statement) throws SQLException {
        ResultSet resultSet = null;
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        preparedStatement.execute();
        resultSet = preparedStatement.getResultSet();
        connection.commit();
        return resultSet;
    }

    public ResultSet requestToDBAndGetGenerated(String statement) throws SQLException {
        ResultSet resultSet = null;
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        preparedStatement.execute();
        resultSet = preparedStatement.getGeneratedKeys();
        connection.commit();
        return resultSet;
    }

    private JSONArray getSteps(Integer recId) {
        JSONArray result = new JSONArray();
        try {
            ResultSet resultSet = requestToDB("Select s.STEP_ID, s.PHOTO, s.DESCRIPTION " +
                    " from STEPS as s where s.REC_ID = " + recId);

            while (resultSet.next()) {
                int step_id = resultSet.getInt("step_id");
                Blob photo = resultSet.getBlob("photo");
                String description = resultSet.getString("description");

                JSONObject step = new JSONObject();
                step.put("photo", photo);
                step.put("step_id", step_id);
                step.put("description", description);
                result.put(step);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //    Список ингридентов в рецете с кол-вом и еденицами измерения
    private JSONArray getDetailsOfReceipt(Integer recId) {
        JSONArray result = new JSONArray();
        try {
            ResultSet resultSet = requestToDB("SELECT rd.INGR_ID, rd.UNIT, " +
                    "rd.COUNT, i.ING_NAME FROM RECEIPT_DETAILS as rd, INGREDIENT as i " +
                    "WHERE rd.INGR_ID = i.INGR_ID AND rd.REC_ID = " + recId);

            while (resultSet.next()) {
                int inr_id = resultSet.getInt("ingr_id");
                String ign_name = resultSet.getString("ing_name");
                String unit = resultSet.getString("unit");
                double count = resultSet.getDouble("count");

                JSONObject ingrInReceipt = new JSONObject();
                ingrInReceipt.put("unit", unit);
                ingrInReceipt.put("ingr_id", inr_id);
                ingrInReceipt.put("ing_name", ign_name);
                ingrInReceipt.put("count", count);
                result.put(ingrInReceipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject getReceiptDetails(Integer recId) {
        JSONObject receiptInDetails = new JSONObject();
        receiptInDetails.put("steps", getSteps(recId));
        receiptInDetails.put("ingredients", getDetailsOfReceipt(recId));
        return receiptInDetails;
    }

//    public JSONObject getReceiptDetails(Integer recId) {
//        JSONObject result = new JSONObject();
//        try {
//            ResultSet resultSet = requestToDB("Select * from RECEIPT, RECEIPT_DETAILS, INGREDIENT, STEPS" +
//                    " where RECEIPT.REC_ID = RECEIPT_DETAILS.REC_ID and INGREDIENT.INGR_ID = RECEIPT_DETAILS.INGR_ID" +
//                    " and RECEIPT.REC_ID = STEPS.REC_ID and RECEIPT.REC_ID = " + recId);
//            if (!resultSet.next())
//                return result;
//            result.put("rec_id", resultSet.getInt("RECEIPT.REC_ID"));
//            result.put("rec_name", resultSet.getString("REC_NAME"));
////            result.put("rating", resultSet.getInt("RATING"));
//            result.put("date", resultSet.getDate("PUBLICATION_DATE"));
//            JSONArray steps = new JSONArray();
//            JSONObject step = new JSONObject();
//            step.put("step_id", resultSet.getInt("STEP_ID"));
//            step.put("description", resultSet.getString("DESCRIPTION"));
//            steps.put(step);
//            result.put("steps", steps);
//
//            JSONArray ingredients = new JSONArray();
//            JSONObject ingredient = new JSONObject();
//            ingredient.put("name", resultSet.getString("ING_NAME"));
//            ingredient.put("count", resultSet.getDouble("COUNT"));
//            ingredient.put("unit", resultSet.getString("UNIT"));
//            ingredients.put(ingredient);
//            result.put("ingredients", ingredients);
//            while (resultSet.next()) {
//                step = new JSONObject();
//                step.put("step_id", resultSet.getInt("STEP_ID"));
//                step.put("description", resultSet.getString("DESCRIPTION"));
//                steps.put(step);
//                ingredient = new JSONObject();
//                ingredient.put("name", resultSet.getString("ING_NAME"));
//                ingredient.put("count", resultSet.getDouble("COUNT"));
//                ingredient.put("unit", resultSet.getString("UNIT"));
//                ingredients.put(ingredient);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }



}
