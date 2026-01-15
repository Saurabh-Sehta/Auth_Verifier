package com.Saurabh.Auth_app.Controller;

import com.Saurabh.Auth_app.Service.ProfileService;
import com.Saurabh.Auth_app.io.ProfileRequest;
import com.Saurabh.Auth_app.io.ProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        try{
            ProfileResponse response = profileService.createProfile(request);

            // TO DO: SEND WELCOME EMAIL

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
