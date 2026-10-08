package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.LoginRequest;
import com.akillisaha.crm.dto.request.RegisterRequest;
import com.akillisaha.crm.dto.response.AuthResponse;
import com.akillisaha.crm.dto.response.UserSummaryResponse;
import com.akillisaha.crm.security.CustomUserDetails;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    UserSummaryResponse getCurrentUser(CustomUserDetails currentUser);
}
