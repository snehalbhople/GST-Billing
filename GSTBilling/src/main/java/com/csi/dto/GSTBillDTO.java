package com.csi.dto;

import jakarta.validation.constraints.Pattern;

public record GSTBillDTO(

        @Pattern(regexp = "[a-zA-Z\\s]{4,30}", message = "Please enter customer name")
        String custName,

        long custContact, String custEmail, String custAddress, String custGSTNO,

        @Pattern(regexp = "^[a-zA-Z+,\\s-]+$", message = "Please enter bill description")
        String billDescription,

        @Pattern(regexp = "[0-9]{1,8}", message = "Please enter total amount in integer format")
        String totalAmount
) {
}