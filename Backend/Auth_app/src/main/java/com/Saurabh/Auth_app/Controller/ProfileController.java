package com.Saurabh.Auth_app.Controller;

import com.Saurabh.Auth_app.Service.EmailService;
import com.Saurabh.Auth_app.Service.ProfileService;
import com.Saurabh.Auth_app.io.ProfileRequest;
import com.Saurabh.Auth_app.io.ProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        try{
            ProfileResponse response = profileService.createProfile(request);

            emailService.setWelcomeEmail(response.getEmail(), response.getName());

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email){
        return profileService.getProfile(email);
    }

//    @GetMapping("/test")
//    public String test(){
//        return "Auth is working";
//    }
}
