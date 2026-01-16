package com.Saurabh.Auth_app.Service;

import com.Saurabh.Auth_app.Entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Saurabh.Auth_app.Repository.UserRepository;
import com.Saurabh.Auth_app.io.ProfileRequest;
import com.Saurabh.Auth_app.io.ProfileResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if (!userRepository.existsByEmail(request.getEmail())){
            newProfile = userRepository.save(newProfile);
            return converttoProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return converttoProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //Calculate expiry time (current time + 15 minutes in milliseconds)
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);

        //Update the prfile/user
        existingUser.setResetOtp(otp);
        existingUser.setResentOtpExpireAt(expiryTime);

        //save into the database
        userRepository.save(existingUser);

        try {
            emailService.sendResetOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        if(existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getResentOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP Expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResentOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()){
            return;
        }

        //Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //Calculate expiry time (current time + 24 hours in milliseconds)
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        //Update the user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);

        //save to database
        userRepository.save(existingUser);

        try{
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {

    }

    @Override
    public String getLoggedInUserId(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        return existingUser.getUserId();
    }

    private ProfileResponse converttoProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resentOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }

}
