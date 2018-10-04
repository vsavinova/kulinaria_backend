package ru.hse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.DBHelper;
import ru.hse.model.Receipt;
import ru.hse.utils.Errors;
import ru.hse.utils.Utils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface CommonService {
    Map<String, Object> getReceipts();

    Map<String, Object> findReceipts(Integer ingrId, Integer cookTime, Integer nutrVal, String recName);

    Map<String, Object> getReceiptDetailsByRecId(Integer recId);

    @Service
    class Impl implements CommonService {
        private DBHelper dbHelper;

        @Autowired
        public Impl(DBHelper dbHelper) {
            this.dbHelper = dbHelper;
        }

        @Override
        public Map<String, Object> getReceipts() {
            Map<String, Object> result;
            try {
                List<Receipt> allReceipts = dbHelper.getAllReceipts();
                result = Utils.createSuccess(allReceipts);
            } catch (SQLException e) {
                result = Utils.createError(Errors.SQL_GET_INFO_ERROR, e);
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public Map<String, Object> findReceipts(Integer ingrId, Integer cookTime, Integer nutrVal, String recName) {
            List<Receipt> receipts = dbHelper.findReceipt(ingrId, cookTime, nutrVal, recName);
            return Utils.createSuccess(receipts);
        }

        @Override
        public Map<String, Object> getReceiptDetailsByRecId(Integer recId) {
            Receipt receiptDetails = dbHelper.getReceiptDetails(recId);
            return Utils.createSuccess(receiptDetails);
        }
    }
}
