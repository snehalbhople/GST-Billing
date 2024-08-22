package com.csi.service.impl;

import com.csi.dao.GSTBillDao;
import com.csi.dto.AllTotalAmounts;
import com.csi.dto.GSTBillDTO;
import com.csi.model.GSTBill;
import com.csi.service.GSTBillService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class GSTBillServiceImpl implements GSTBillService {

    @Autowired
    private GSTBillDao gstBillDao;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Value("${local.disk.path}")
    private String localPath;

    @Override
    public GSTBill generateNewBill(GSTBillDTO dto) {
        GSTBill gstBill = new GSTBill();
        // ID Generation
        if (gstBillDao.getAllBills().size() != 0) {
            String id = gstBillDao.getAllBills().stream().map(GSTBill::getInvoiceId).sorted(Comparator.comparing(String::valueOf).reversed()).toList().get(0);
            int newId = Integer.parseInt(id.replaceAll("[^0-9]", ""));
            gstBill.setInvoiceId("CS-" + (newId + 1));
        } else {
            gstBill.setInvoiceId("CS-100");
        }
        gstBill.setCustName(dto.custName());
        gstBill.setBillDate(new Date());
        gstBill.setCustContact(dto.custContact());
        gstBill.setCustEmail(dto.custEmail());
        gstBill.setCustAddress(dto.custAddress());
        gstBill.setCustGSTNO(dto.custGSTNO());
        gstBill.setBillDescription(dto.billDescription());
        gstBill.setTotalAmount(Integer.parseInt(dto.totalAmount()));
        // GST Calculation
        double gst = Integer.parseInt(dto.totalAmount()) * ((double) 18 / (100 + 18));
        gstBill.setCgstAmount((int) Math.round(gst / 2));
        gstBill.setSgstAmount((int) Math.round((gst / 2)));
        gstBill.setAmount(Integer.parseInt(dto.totalAmount()) - gstBill.getCgstAmount() - gstBill.getSgstAmount());
        return gstBillDao.generateNewBill(gstBill);
    }

    @Override
    public GSTBill getBillById(String invoiceId) {
        return gstBillDao.getBillById(invoiceId);
    }

    @Override
    public List<GSTBill> getAllBills() {
        return gstBillDao.getAllBills();
    }

    @Override
    public void deleteBillById(String invoiceId) {
        gstBillDao.deleteBillById(invoiceId);
    }

    @Override
    public String sendInvoiceToEmail(String invoiceId, MultipartFile file) throws IOException {
        GSTBill bill = gstBillDao.getBillById(invoiceId);
        String fileName = bill.getCustName() + " Invoice " + bill.getInvoiceId() + ".pdf";

        Files.copy(file.getInputStream(), Paths.get(localPath + fileName), StandardCopyOption.REPLACE_EXISTING);
        File savedFile = new File(localPath + fileName);
        FileSystemResource resource = new FileSystemResource(savedFile);

        String emailMessage = """
                <div style='background-color: #f7bae4; margin: 15px; padding: 20px 20px 20px 30px'>
                    <p>Hi <b>%s</b>,</p>
                    <div style='padding-left:10px;'>
                        <p style='margin-bottom: -10px'>Thank you for your recent business with us. We have attached detail copy of invoice <b>'<i>%s</i>'</b> to this mail.</p>
                        <p style='margin-bottom: -10px'>The invoice total is <b>Rupees %d</b> paid to <b><i>%s</i></b>.</p>
                        <p >If you have any question or concerns regarding this invoice, please don't hesitate to get in touch with us at <b><i>contact@fullstackjavadeveloper.in</i></b>.</p>
                    </div>
                    <p style='margin-bottom: -10px'>Thanks,</p>
                   <p style='margin-bottom: -10px'><b><i>AA</i></b></p>
                   <p>Director</p>
                </div>
                """.formatted(bill.getCustName(), fileName, bill.getTotalAmount(), bill.getBillDate());
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setSubject("FULLSTACK Invoice");
            mimeMessageHelper.setText(emailMessage, true);
            mimeMessageHelper.setFrom(fromMail, "FULL STACK JAVA DEVELOPER PUNE Pvt Ltd");
            mimeMessageHelper.setTo(bill.getCustEmail());
            mimeMessageHelper.addAttachment(fileName, resource);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return "Invoice send successfully to " + bill.getCustName();
    }

    @Override
    public List<GSTBill> sortBills(String value) {
        return switch (value) {
            case "invoiceIDDesc" ->
                    gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getInvoiceId).reversed()).toList();
            case "custNameAsc" ->
                    gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getCustName)).toList();
            case "custNameDesc" ->
                    gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getCustName).reversed()).toList();
            case "billDateAsc" ->
                    gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getBillDate)).toList();
            case "billDateDesc" ->
                    gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getBillDate).reversed()).toList();
            default -> gstBillDao.getAllBills().stream().sorted(Comparator.comparing(GSTBill::getInvoiceId)).toList();
        };
    }

    @Override
    public List<GSTBill> searchByAnyInput(String input) {
        List<GSTBill> gstBills = new ArrayList<>();

        if (input.matches("[a-zA-Z\\s]{4,30}")) {                                                   // Customer Name
            gstBillDao.getAllBills().stream().filter(gstBill -> gstBill.getCustName().equalsIgnoreCase(input)).forEach(gstBills::add);
        } else if (input.matches("^[A-Z+-]{2,3}+[0-9]{3,6}+$")) {
            gstBillDao.getAllBills().stream().filter(gstBill -> gstBill.getInvoiceId().equals(input)).forEach(gstBills::add);
        } else if (input.matches("^[0-9]{10}+$")) {                                                 // Contact Number
            gstBillDao.getAllBills().stream().filter(gstBill -> gstBill.getCustContact() == Long.parseLong(input)).forEach(gstBills::add);
        } else if (input.matches("^[a-z0-9+.]+@[a-z]+[(.){1}]+[a-z]+$")) {                           // Email
            gstBillDao.getAllBills().stream().filter(gstBill -> gstBill.getCustEmail().equals(input)).forEach(gstBills::add);
        } else if (input.matches("(0[1-9]|1[0-9]|2[0-9]|3[0-1]|[1-9])-(0[1-9]|1[0-2]|[1-9])-([0-9]{4})")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");              // Invoice Date
            if (input.matches("([0-9]{2})-([0-9]{2})-([0-9]{4})")) {         // dd-MM-yyyy
                gstBillDao.getAllBills().stream().filter(gstBill -> simpleDateFormat.format(gstBill.getBillDate()).equals(input)).forEach(gstBills::add);
            } else if (input.matches("([0-9]{1})-([0-9]{1})-([0-9]{4})")) {  // d-M-yyyy
                String start = input.substring(0, 2);
                String last = input.substring(2, 8);
                String newInput = "0" + start + "0" + last;
                gstBillDao.getAllBills().stream().filter(gstBill -> simpleDateFormat.format(gstBill.getBillDate()).equals(newInput)).forEach(gstBills::add);
            } else if (input.matches("([0-9]{2})-([0-9]{1})-([0-9]{4})")) {   // dd-M-yyyy
                String start = input.substring(0, 3);
                String last = input.substring(3, 9);
                String newInput = start + "0" + last;
                gstBillDao.getAllBills().stream().filter(gstBill ->
                        simpleDateFormat.format(gstBill.getBillDate()).equals(newInput)).forEach(gstBills::add);
            } else {                                                                 // d-MM-yyyy
                gstBillDao.getAllBills().stream().filter(gstBill -> simpleDateFormat.format(gstBill.getBillDate()).equals("0" + input)).forEach(gstBills::add);
            }
        }
        return gstBills;
    }

    @Override
    public AllTotalAmounts getAllTotalAmounts() {
        int allAmount = gstBillDao.getAllBills().stream().map(GSTBill::getAmount).reduce(0, Integer::sum);
        int allCgstAmount = gstBillDao.getAllBills().stream().map(GSTBill::getCgstAmount).reduce(0, Integer::sum);
        int allSgstAmount = gstBillDao.getAllBills().stream().map(GSTBill::getSgstAmount).reduce(0, Integer::sum);
        int allTotalAmount = gstBillDao.getAllBills().stream().map(GSTBill::getTotalAmount).reduce(0, Integer::sum);
        return new AllTotalAmounts(allAmount, allCgstAmount, allSgstAmount, allTotalAmount);
    }

    @Override
    public GSTBill updateBillByID(String invoiceId, GSTBillDTO dto) {
        GSTBill gstBill = gstBillDao.getBillById(invoiceId);
        gstBill.setCustName(dto.custName());
        gstBill.setCustContact(dto.custContact());
        gstBill.setCustEmail(dto.custEmail());
        gstBill.setCustAddress(dto.custAddress());
        gstBill.setCustGSTNO(dto.custGSTNO());
        gstBill.setBillDescription(dto.billDescription());
        gstBill.setTotalAmount(Integer.parseInt(dto.totalAmount()));
        // GST Calculation
        double gst = Integer.parseInt(dto.totalAmount()) * ((double) 18 / (100 + 18));
        gstBill.setCgstAmount((int) Math.round(gst / 2));
        gstBill.setSgstAmount((int) Math.round((gst / 2)));
        gstBill.setAmount(Integer.parseInt(dto.totalAmount()) - gstBill.getCgstAmount() - gstBill.getSgstAmount());
        return gstBillDao.generateNewBill(gstBill);
    }
}