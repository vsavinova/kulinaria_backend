package ru.hse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.model.Receipt;
import ru.hse.model.User;
import ru.hse.model.UserInfo;
import ru.hse.model.Voting;
import ru.hse.service.CommonService;
import ru.hse.service.TokenService;
import ru.hse.service.UserService;
import ru.hse.utils.Errors;
import ru.hse.utils.Utils;

import java.util.List;
import java.util.Map;

public interface Facade {
    Map<String, Object> getReceipts();

    Map<String, Object> getReceiptsByUser(Integer userId);

    Map<String, Object> getFavoriteByUser(Integer userId, String token);

    Map<String, Object> addFavoriteToUser(Integer userId, Integer recId, String token);

    Map<String, Object> getUserInfo(Integer userId);

    Map<String, Object> getReceiptDetailsByRecId(Integer recId);

    Map<String, Object> findReceipts(Integer ingrId, Integer cookTime, Integer nutrVal, String recName);

    Map<String, Object> auth(User user);

    Map<String, Object> register(User user);

    Map<String, Object> addUserInfo(UserInfo userInfo);

    Map<String, Object> createReceipt(Receipt receipt);

    Map<String, Object> addMark(Voting voting);

    @Service
    class Impl implements Facade {
        private UserService userService;
        private TokenService tokenService;
        private CommonService commonService;

        @Autowired
        public Impl(UserService userService, TokenService tokenService, CommonService commonService) {
            this.userService = userService;
            this.tokenService = tokenService;
            this.commonService = commonService;
        }


        @Override
        public Map<String, Object> getReceipts() {
            return commonService.getReceipts();
        }

        @Override
        public Map<String, Object> getReceiptsByUser(Integer userId) {
            return Utils.createSuccess(userId, userService.getUserReceipts(userId));
        }

        @Override
        public Map<String, Object> getFavoriteByUser(Integer userId, String token) {
            token = Utils.reformatToken(token);
            boolean checkToken = tokenService.checkToken(token, userId);
            if (!checkToken)
                return Utils.createError(Errors.INVALID_TOKEN, token);
            List<Receipt> favorite = userService.getFavoriteByUserId(userId);
            return Utils.createSuccess(token, favorite);
        }

        @Override
        public Map<String, Object> addFavoriteToUser(Integer userId, Integer recId, String token) {
            Map<String, Object> result;
            token = Utils.reformatToken(token);
            boolean checkToken = tokenService.checkToken(token, userId);
            if (checkToken)
                result = userService.addFavoriteBToUser(userId, recId);
            else
                result = Utils.createError(Errors.INVALID_TOKEN, token);
            return result;
        }

        @Override
        public Map<String, Object> getUserInfo(Integer userId) {
            return userService.getUserInfo(userId);
        }

        @Override
        public Map<String, Object> getReceiptDetailsByRecId(Integer recId) {
            return commonService.getReceiptDetailsByRecId(recId);
        }

        @Override
        public Map<String, Object> findReceipts(Integer ingrId, Integer cookTime, Integer nutrVal, String recName) {
            return commonService.findReceipts(ingrId, cookTime, nutrVal, recName);
        }

        @Override
        public Map<String, Object> auth(User user) {
            Map<String, Object> result;
            User auth = userService.auth(user.getLogin(), user.getPassword());
            if (auth != null)
                result = Utils.createSuccess(tokenService.getToken(auth), auth.getUserId(), user);
            else
                result = Utils.createError(Errors.AUTH_ERROR);
            return result;
        }

        @Override
        public Map<String, Object> register(User user) {
            String token = tokenService.getToken(user);
            return userService.register(token, user.getLogin(), user.getPassword());
        }

        @Override
        public Map<String, Object> addUserInfo(UserInfo userInfo) {
            Map<String, Object> result;
            boolean checkToken = tokenService.checkToken(userInfo.getToken(), userInfo.getUserId());
            if (checkToken) {
                result = userService.addUserInfo(userInfo);
            } else
                result = Utils.createError(Errors.INVALID_TOKEN, userInfo.getToken());
            return result;
        }

        @Override
        public Map<String, Object> createReceipt(Receipt receipt) {
            Map<String, Object> result;
            boolean checkToken = tokenService.checkToken(receipt.getToken(), receipt.getUserId());
            if (checkToken) {
                result = userService.createReceipt(receipt);
            } else
                result = Utils.createError(Errors.INVALID_TOKEN, receipt.getToken());
            return result;
        }

        @Override
        public Map<String, Object> addMark(Voting voting) {
            Map<String, Object> result;
            boolean checkToken = tokenService.checkToken(voting.getToken(), voting.getUserId());
            if (checkToken) {
                result = userService.addMark(voting);
            } else
                result = Utils.createError(Errors.INVALID_TOKEN, voting.getToken());
            return result;
        }
    }

}
