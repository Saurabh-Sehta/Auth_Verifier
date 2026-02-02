package com.Saurabh.Auth_app.Service;

import com.Saurabh.Auth_app.Entity.EmailEntity;
import com.Saurabh.Auth_app.Entity.UserEntity;
import com.Saurabh.Auth_app.Repository.EmailRepository;
import jakarta.mail.MessagingException;
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
    private final EmailRepository emailRepository;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {

        String email = request.getEmail();

        // 1️⃣ Check email verification record
        EmailEntity emailEntity = emailRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is not verified"));

        if (!Boolean.TRUE.equals(emailEntity.getIsAccountVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is not verified");
        }

        // 2️⃣ Check if profile already exists
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // 3️⃣ Create profile
        UserEntity newProfile = convertToUserEntity(request);
        newProfile = userRepository.save(newProfile);

        return converttoProfileResponse(newProfile);
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
            emailService.sendResetOtpEmail(existingUser.getEmail(), existingUser.getName(), otp);
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

        if (userRepository.existsByEmail(email)){
            throw new RuntimeException("User with this email address already exists.");
        }

        EmailEntity user;

        if (emailRepository.existsByEmail(email)) {
            user = emailRepository.findByEmail(email).orElseThrow();
        } else {
            user = EmailEntity.builder()
                    .email(email)
                    .verifyOtp(null)
                    .verifyOtpExpireAt(0L)
                    .otpSentAt(0L)
                    .build();
        }

        long now = System.currentTimeMillis();

        // 1️⃣ Cooldown check (60 seconds)
        if (user.getOtpSentAt() != null && (now - user.getOtpSentAt()) < 60_000) {
            throw new RuntimeException("Please wait before requesting OTP again");
        }

        // 2️⃣ Reuse OTP if still valid
        String otp;
        if (user.getVerifyOtp() != null && user.getVerifyOtpExpireAt() > now) {
            otp = user.getVerifyOtp();   // resend same OTP
        } else {
            otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            user.setVerifyOtp(otp);
            user.setVerifyOtpExpireAt(now + (5 * 60 * 1000)); // 5 minutes
        }
        try {
            // 3️⃣ Send email
            emailService.sendOtpEmail(user.getEmail(), otp);

            // 4️⃣ Update timestamp & save
            user.setOtpSentAt(now);
            emailRepository.save(user);
        } catch (RuntimeException | MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void verifyOtp(String email, String otp) {
        EmailEntity existingUser;
        if (emailRepository.existsByEmail(email)){
            existingUser = emailRepository.findByEmail(email).orElseThrow();
            if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)){
                throw new RuntimeException("Invalid OTP");
            }
            if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
                throw new RuntimeException("OTP Expired");

            }
            existingUser.setIsAccountVerified(true);
            existingUser.setVerifyOtpExpireAt(0L);
            existingUser.setVerifyOtp(null);
            emailRepository.save(existingUser);
        } else {
            throw new RuntimeException("Didn't get this email address");
        }
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
                .isAccountVerified(true)
                .resentOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }

}
