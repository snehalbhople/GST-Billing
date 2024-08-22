package com.csi.controller;

import com.csi.dto.AllTotalAmounts;
import com.csi.dto.ErrorResponse;
import com.csi.dto.GSTBillDTO;
import com.csi.model.GSTBill;
import com.csi.service.GSTBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/v1/gstbills")
@CrossOrigin(origins = "http://localhost:9002", allowCredentials = "true")
@Tag(name = "GST Billing", description = "APIS Of GST Bill Controller")
@SecurityRequirement(name = "Bearer Auth")
public class GSTBillController {

    @Autowired
    private GSTBillService gstBillService;

    @PostMapping("/")
    @Operation(summary = "Generate New GST Bill", description = "Api for generate new gst bill. Customer name, total amount and bill description is must",
            responses = {
                    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = GSTBill.class))),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = LinkedHashMap.class))),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header")))})
    public ResponseEntity<GSTBill> generateNewBill(@Valid @RequestBody GSTBillDTO dto) {
        return new ResponseEntity<>(gstBillService.generateNewBill(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get Bill By ID", description = "Provide valid invoice id to get bill", responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GSTBill.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<GSTBill> getBillById(@PathVariable String invoiceId) {
        return ResponseEntity.ok(gstBillService.getBillById(invoiceId));
    }

    @GetMapping("/")
    @Operation(summary = "Get All Bills", description = "API for get all bills from database", responses =
    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))))
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GSTBill>> getAllBills() {
        return ResponseEntity.ok(gstBillService.getAllBills());
    }

    @DeleteMapping("/{invoiceId}")
    @Operation(summary = "Delete Bill By ID", description = "Provide valid invoice id to delete the bill", responses = {
            @ApiResponse(responseCode = "204", content = @Content(schema = @Schema(example = "GST bill deleted successfully"))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> deleteBillById(@PathVariable String invoiceId) {
        gstBillService.deleteBillById(invoiceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(value = "/mail-invoice/{invoiceId}", consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Email The Invoice", description = "API for sending mail with invoice to customer. Provide valid invoice id. Select multipart form data and upload invoice pdf",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<String> sendInvoiceToEmail(@PathVariable String invoiceId, @RequestPart("pdf") MultipartFile file) throws IOException {
        return ResponseEntity.ok(gstBillService.sendInvoiceToEmail(invoiceId, file));
    }

    @GetMapping("/sort")
    @Operation(summary = "Sort Bills", description = "API for sorting bills. Provide - invoiceIDDesc / custNameAsc / custNameDesc " +
            "/ billDateAsc / billDateDesc as a value of request param. If not provided it works as a invoiceIDAsc",
            responses = @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))))
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GSTBill>> sortBills(@RequestParam(required = false, defaultValue = "") String value) {
        return ResponseEntity.ok(gstBillService.sortBills(value));
    }

    @GetMapping("/search/{input}")
    @Operation(summary = "Search Bill Using Any Input", description = "API for search bill using any input. " +
            "Input should be Customer Name / Invoice ID / Contact Number / Email / Invoice Date",
            responses = @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))))
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GSTBill>> searchByAnyInput(@PathVariable String input) {
        return ResponseEntity.ok(gstBillService.searchByAnyInput(input));
    }

    @GetMapping("/all-totals")
    @Operation(summary = "Total Of Each Amount", description = "API for sum of all amounts", responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AllTotalAmounts.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header")))})
    public ResponseEntity<AllTotalAmounts> getAllTotalAmounts() {
        return ResponseEntity.ok(gstBillService.getAllTotalAmounts());
    }

    @PutMapping("/{invoiceId}")
    @Operation(summary = "Update GST Bill", description = "Api for update existing gst bill. Customer name, total amount and bill description is must",
            responses = {
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = LinkedHashMap.class))),
                    @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(example = "Unauthorized. Please login and provide token in header"))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GSTBill> updateBillByID(@PathVariable String invoiceId, @Valid @RequestBody GSTBillDTO gstBillDTO) {
        return ResponseEntity.ok(gstBillService.updateBillByID(invoiceId, gstBillDTO));
    }
}