package com.csi.controller;

import com.csi.config.JWTUtil;
import com.csi.dto.ErrorResponse;
import com.csi.dto.SignInRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "http://localhost:9002", allowCredentials = "true")
@Tag(name = "Auth", description = "API Of signin request ")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/signin")
    @Operation(summary = "Sign In", description = "Signin using userName and password as 'Admin'. Click on Authorized button and paste the token for accessing GST Controller",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<String> generateToken(@RequestBody SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.userName(), signInRequest.password()));
        return ResponseEntity.ok(jwtUtil.generateToken(signInRequest.userName()));
    }
}