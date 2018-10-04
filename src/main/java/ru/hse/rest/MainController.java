package ru.hse.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.hse.Facade;
import ru.hse.model.Receipt;
import ru.hse.model.User;
import ru.hse.model.UserInfo;
import ru.hse.model.Voting;

import java.util.Map;

@RestController
public class MainController {

    @Autowired
    Facade facade;

    @GetMapping(value = "/test")
    public String test() {
        return "hello";
    }

    @GetMapping(value = "/get_receipts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getReceipts() {
        return facade.getReceipts();
    }


    @GetMapping(value = "/get_receipts_by_user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getReceiptsByUser(@RequestParam(name = "user_id") Integer userId) {
        return facade.getReceiptsByUser(userId);
    }

    @PostMapping(value = "/get_favorite_by_user",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getFavoriteByUser(@RequestParam(name = "user_id") Integer userId,
                                                 @RequestBody String token) {
        return facade.getFavoriteByUser(userId, token);
    }


    @PostMapping(value = "/add_favorite_to_user",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> addFavoriteToUser(@RequestParam(name = "user_id") Integer userId,
                                                 @RequestParam(name = "rec_id") Integer recId,
                                                 @RequestBody String token) {
        return facade.addFavoriteToUser(userId, recId, token);
    }

    @GetMapping(value = "/get_user_info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getUserInfo(@RequestParam(name = "user_id") Integer userId) {
        return facade.getUserInfo(userId);
    }

    @GetMapping(value = "/get_receipt_details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getReceiptDetailsByRecId(@RequestParam(name = "rec_id") Integer recId) {
        return facade.getReceiptDetailsByRecId(recId);
    }


    @GetMapping(value = "/find_receipts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> findReceipts(
            @RequestParam(name = "ingr_id", required = false) Integer ingrId,
            @RequestParam(name = "cook_time", required = false) Integer cookTime,
            @RequestParam(name = "nutr_val", required = false) Integer nutrVal,
            @RequestParam(name = "rec_name", required = false) String recName
    ) {
        return facade.findReceipts(ingrId, cookTime, nutrVal, recName);
    }


    @PostMapping(value = "/auth",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> auth(@RequestBody User user) {
        return facade.auth(user);
    }


    @PostMapping(value = "/register",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> register(@RequestBody User user) {
        return facade.register(user);
    }


    @PostMapping(value = "/addUserInfo",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> addUserInfo(@RequestBody UserInfo userInfo) {
        return facade.addUserInfo(userInfo);
    }

    @PostMapping(value = "/add_mark_to_receipt",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> addMarksToReceipt(@RequestBody Voting voting) {
        return facade.addMark(voting);
    }


    @PostMapping(value = "/create_receipt",
            consumes = "application/json",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> createReceipt(@RequestBody Receipt receipt) {
        return facade.createReceipt(receipt);
    }

}
