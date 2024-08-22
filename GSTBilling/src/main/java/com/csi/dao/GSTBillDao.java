package com.csi.dao;

import com.csi.model.GSTBill;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GSTBillDao {

    @CacheEvict(value = {"AllBills", "BilByID"}, allEntries = true)
    GSTBill generateNewBill(GSTBill gstBill);

    @Cacheable(value = "BilByID")
    GSTBill getBillById(String invoiceId);

    @Cacheable(value = "AllBills")
    List<GSTBill> getAllBills();

    @CacheEvict(value = {"AllBills", "BilByID"}, allEntries = true)
    void deleteBillById(String invoiceId);
}