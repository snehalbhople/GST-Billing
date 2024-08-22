package com.csi.dao.impl;

import com.csi.dao.GSTBillDao;
import com.csi.exception.GSTBillNotFound;
import com.csi.model.GSTBill;
import com.csi.repository.GSTBillRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GSTBillDaoImpl implements GSTBillDao {

    @Autowired
    private GSTBillRepo gstBillRepo;

    @Override
    public GSTBill generateNewBill(GSTBill gstBill) {
        return gstBillRepo.save(gstBill);
    }

    @Override
    public GSTBill getBillById(String invoiceId) {
        return gstBillRepo.findById(invoiceId).orElseThrow(() -> new GSTBillNotFound("GST bill having id " + invoiceId + " is not found"));
    }

    @Override
    public List<GSTBill> getAllBills() {
        return gstBillRepo.findAll();
    }

    @Override
    public void deleteBillById(String invoiceId) {
        GSTBill bill = getBillById(invoiceId);
        gstBillRepo.delete(bill);
    }
}