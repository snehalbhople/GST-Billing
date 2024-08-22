package com.csi.dto;

public record ErrorResponse(String message, Object cause, int statusCode, String status) {
}