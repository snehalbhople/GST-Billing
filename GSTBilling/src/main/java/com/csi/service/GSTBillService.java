package com.csi.service;

import com.csi.dto.AllTotalAmounts;
import com.csi.dto.GSTBillDTO;
import com.csi.model.GSTBill;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface GSTBillService {

    GSTBill generateNewBill(GSTBillDTO dto);

    GSTBill getBillById(String invoiceId);

    List<GSTBill> getAllBills();

    void deleteBillById(String invoiceId);

    String sendInvoiceToEmail(String invoiceId, MultipartFile file) throws IOException;

    List<GSTBill> sortBills(String value);

    List<GSTBill> searchByAnyInput(String input);

    AllTotalAmounts getAllTotalAmounts();

    GSTBill updateBillByID(String invoiceId, GSTBillDTO gstBillDTO);
}