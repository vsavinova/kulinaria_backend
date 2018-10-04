package ru.hse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hse.model.Ingredient;
import ru.hse.model.Receipt;
import ru.hse.model.Step;
import ru.hse.model.UserInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
public class DBHelper {

    private DataSource dataSource;
    Connection connection;


    @Autowired
    public DBHelper(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
    }

    public List<Receipt> getAllReceipts() throws SQLException {
        List<Receipt> receipts = new ArrayList<>();
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
            String photo = resultSet.getString("PHOTO");
            Integer cooking_time = resultSet.getInt("TIME");
            double rating = resultSet.getDouble("RATING");
            String nutr_val_name = resultSet.getString("NUTR_VAL_NAME");
            String category = resultSet.getString("CAT_NAME");
            Receipt receipt = new Receipt();
            receipt.setRecName(recName);
            receipt.setPhoto(photo);
            receipt.setTime(cooking_time);
            receipt.setRating(rating);
            receipt.setDifficulty(nutr_val_name);
            receipt.setCategory(category);
            receipts.add(receipt);
        }
        return receipts;
    }

    private int getReceptsCount(Integer userId) throws SQLException {
        ResultSet resultSet = requestToDB("SELECT count(r.REC_ID) as receipts FROM RECEIPT " +
                "as r where r.USER_ID = " + userId);
        if (resultSet.next())
            return resultSet.getInt("receipts");
        else
            return 0;
    }

    public UserInfo getUserInfo(Integer userId) {
        UserInfo userInfo = new UserInfo();
        try {
            ResultSet resultSet = requestToDB(String.format("SELECT u.USER_NAME, u.PHOTO, u.BIO, " +
                            "u.REGISTRATION_DATE as rd from USER as u where u.USER_ID = %d",
                    userId, userId));
            if (resultSet.next()) {
                String name = resultSet.getString("user_name");
                Blob userPhoto = resultSet.getBlob("photo");
                String story = resultSet.getString("bio");
                Date date = resultSet.getDate("rd");
                userInfo.setUserId(userId);
                userInfo.setUserName(name);
                userInfo.setPhoto(userPhoto);
                userInfo.setBio(story);
                userInfo.setRegistrationDate(date);
            }
            int receipts = getReceptsCount(userId);
            userInfo.setReceiptsCount(receipts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userInfo;
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

    public List<Receipt> findReceipt(Integer ingrId, Integer cookTime, Integer nutrVal, String recName) {
        ArrayList<Receipt> receipts = new ArrayList<>();
        try {
            String statement =
                    "Select r.REC_ID, r.REC_NAME, r.PHOTO, r.ANNOTATION, nv.NUTR_VAL_NAME, " +
                            " nv.NUTR_ID, r.TIME, c.CAT_NAME from RECEIPT as r, CATEGORY as c, " +
                            "NUTRITIONAL_VALUE as nv WHERE  r.NUTR_ID = nv.NUTR_ID AND c.CAT_ID = r.CAT_ID ";
            ArrayList<Integer> recIdsWithIngr = new ArrayList<>();
            if (ingrId != null) {
                recIdsWithIngr = getReceipitIdswithIngr(ingrId);
            }
            if (cookTime != null)
                statement += " AND r.TIME  <= \'" + cookTime + "\'";
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
                String photo = resultSet.getString("r.photo");
                String annotation = resultSet.getString("annotation");
                int cooking_time = resultSet.getInt("time");
                String nutr_val_name = resultSet.getString("nutr_val_name");

                Receipt receipt = new Receipt();
                receipt.setRecName(rec_name);
                receipt.setPhoto(photo);
                receipt.setRecId(rec_id);
                receipt.setRating(rating);
                receipt.setTime(cooking_time);
                receipt.setDifficulty(nutr_val_name);
                receipt.setAnnotation(annotation);
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    public int createReceipt(Receipt receipt) throws SQLException {
        String photo = receipt.getPhoto();
        Integer cooking_time = receipt.getTime();
        String statement1 = String.format("INSERT INTO RECEIPT (REC_NAME, USER_ID, PUBLICATION_DATE," +
                        "                     PHOTO, ANNOTATION, TIME, NUTR_ID, CAT_ID)" +
                        "VALUES ('%s', %d, '" + new Date(System.currentTimeMillis()) + "', " +
                        (photo == null ? null : "'" + photo + "'") + ", '%s', "
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
            return -1;
        return 0;
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

    private List<Step> getSteps(Integer recId) {
        ArrayList<Step> steps = new ArrayList<>();
        try {
            ResultSet resultSet = requestToDB("Select s.STEP_ID, s.PHOTO, s.DESCRIPTION " +
                    " from STEPS as s where s.REC_ID = " + recId);

            while (resultSet.next()) {
                int step_id = resultSet.getInt("step_id");
                String photo = resultSet.getString("photo");
                String description = resultSet.getString("description");

                Step step = new Step();
                step.setPhoto(photo);
                step.setDescription(description);
                step.setStepId(step_id);
                steps.add(step);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return steps;
    }

    //    Список ингридентов в рецете с кол-вом и еденицами измерения
    private List<Ingredient> getDetailsOfReceipt(Integer recId) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        try {
            ResultSet resultSet = requestToDB("SELECT rd.INGR_ID, rd.UNIT, " +
                    "rd.COUNT, i.ING_NAME FROM RECEIPT_DETAILS as rd, INGREDIENT as i " +
                    "WHERE rd.INGR_ID = i.INGR_ID AND rd.REC_ID = " + recId);

            while (resultSet.next()) {
                int inr_id = resultSet.getInt("ingr_id");
                String ign_name = resultSet.getString("ing_name");
                String unit = resultSet.getString("unit");
                double count = resultSet.getDouble("count");

                Ingredient ingredient = new Ingredient();
                ingredient.setUnit(unit);
                ingredient.setIngrId(inr_id);
                ingredient.setIgnName(ign_name);
                ingredient.setCount(count);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public Receipt getReceiptDetails(Integer recId) {
        Receipt receipt = new Receipt();
        receipt.setRecId(recId);
        receipt.setSteps(getSteps(recId));
        receipt.setIngredients(getDetailsOfReceipt(recId));
        return receipt;
    }

}
