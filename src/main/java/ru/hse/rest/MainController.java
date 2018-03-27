package ru.hse.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import ru.hse.Config;
import ru.hse.DBHelper;
import ru.hse.model.Receipt;
import ru.hse.model.User;
import ru.hse.model.UserInfo;
import ru.hse.model.Voting;
import ru.hse.service.TockenManager;
import ru.hse.service.UserService;
import ru.hse.utils.Errors;
import ru.hse.utils.Utils;

@Controller
public class MainController {
    @Autowired
    UserService userService;

//    @Autowired
//    Config config;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test() {
        return "hello";
    }

    @RequestMapping(value = "/get_receipts", method = RequestMethod.GET)
    @ResponseBody
    public String getReceipts() {
        DBHelper helper = userService.getHelper();
        JSONArray allReceipts = helper.getAllReceipts();
        return allReceipts.toString();
    }


    @RequestMapping(value = "/get_receipts_by_user", method = RequestMethod.GET)
    @ResponseBody
    public String getReceiptsByUser(@RequestParam(name = "user_id") Integer userId) {
        JSONArray allReceipts = userService.getUserReceipts(userId);
        return allReceipts.toString();
    }

//    @RequestMapping(value = "/get_favorite_by_user", method = RequestMethod.GET)
//    @ResponseBody
//    public String getFavoriteByUser(@RequestParam(name = "user_id") Integer userId) {
//        DBHelper helper = userService.getHelper();
//        JSONArray favorite = helper.getFavoriteByUserId(userId);
//        return favorite.toString();
//    }


    @PostMapping(value = "/get_favorite_by_user",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public String getFavoriteByUser(@RequestParam(name = "user_id") Integer userId,
                                    @RequestBody String token) {
//        String tokenString = token.substring(token.lastIndexOf(":") + 1);
//        if (tokenString.contains(" "))
//            tokenString = tokenString.replace(" ", "");
        token = Utils.getToken(token);
        boolean checkToken = TockenManager.checkToken(token, userId);
        if (!checkToken)
            return Utils.createError(Errors.INVALID_TOKEN.getMsg(), "").toString();
//        DBHelper helper = userService.getHelper();
        JSONArray favorite = userService.getFavoriteByUserId(userId);
        return favorite.toString();
    }


    @PostMapping(value = "/add_favorite_to_user",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    public String addFavoriteToUser(@RequestParam(name = "user_id") Integer userId,
                                    @RequestParam(name = "rec_id") Integer recId,
                                    @RequestBody String token) {
        return userService.addFavoriteBToUser(token, userId, recId);
    }

    @RequestMapping(value = "/get_user_info", method = RequestMethod.GET,
            produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getUserInfo(@RequestParam(name = "user_id") Integer userId) {
        DBHelper helper = userService.getHelper();
        JSONObject userInfo = helper.getUserInfo(userId);
        return userInfo.toString();
    }

    @RequestMapping(value = "/get_receipt_details", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getReceiptDetailsByRecId(@RequestParam(name = "rec_id") Integer recId) {
        DBHelper helper = userService.getHelper();
        JSONObject receipt = helper.getReceiptDetails(recId);
        return receipt.toString();
    }


    @RequestMapping(value = "/find_receipts", method = RequestMethod.GET)
    @ResponseBody
    public String findReceipts(
            @RequestParam(name = "ingr_id", required = false) Integer ingrId,
            @RequestParam(name = "cook_time", required = false) Integer cookTime,
            @RequestParam(name = "nutr_val", required = false) Integer nutrVal,
            @RequestParam(name = "rec_name", required = false) String recName
    ) {
        DBHelper helper = userService.getHelper();
        JSONArray receipts = helper.findReceipt(ingrId, cookTime, nutrVal, recName);
        return receipts.toString();
    }


    @PostMapping(value = "/auth",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String auth(@RequestBody User user) {
        User auth = userService.auth(user.getLogin(), user.getPassword());
        String token = null;
        if (auth != null)
            token = Utils.createSuccess(TockenManager.getToken(auth), auth.getUserId());
        else
            token = Utils.createError("authentication failed", "").toString();
        return token;
    }


    @PostMapping(value = "/register",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String register(@RequestBody User user) {
        return userService.register(user.getLogin(), user.getPassword());
    }


    @PostMapping(value = "/addUserInfo",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String addUserInfo(@RequestBody UserInfo userInfo) {
        return userService.addUserInfo(userInfo);
    }

    @PostMapping(value = "/add_mark_to_receipt",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String addMarksToReceipt(@RequestBody Voting voting) {
        return userService.addMark(voting);
    }


    @PostMapping(value = "/create_receipt",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String createReceipt(@RequestBody Receipt receipt) {
        return userService.createReceipt(receipt);
    }


    @PostMapping(value = "/update_user_info",
            consumes = "application/json",
            produces = "application/json")
    @ResponseBody
    // возвращает токен, если успешно
    public String updateUserInfo(@RequestBody UserInfo user) {
        return userService.updateInfo(user);
    }




//    @PostMapping(value = "/auth",
//            consumes = "application/json",
//            produces = "application/json")
//    @ResponseBody
//    // возвращает токен, если успешно
//    public User auth(@RequestBody User user) {
//        User auth = userService.auth(user.getLogin(), user.getPassword());
//        return auth;
//    }

}
