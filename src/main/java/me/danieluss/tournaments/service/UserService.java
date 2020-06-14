package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.dto.ResetPassword;
import me.danieluss.tournaments.data.model.AppUser;
import me.danieluss.tournaments.data.model.ConfirmationToken;
import me.danieluss.tournaments.data.repo.ConfirmationTokenRepository;
import me.danieluss.tournaments.data.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserService {

    private final TokenProperties tokenProperties;

    private final UserRepository userRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final EmailService emailService;
    private final URLService urlService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(TokenProperties tokenProperties, UserRepository userRepository,
                       ConfirmationTokenRepository confirmationTokenRepository, EmailService emailService,
                       URLService urlService, PasswordEncoder passwordEncoder) {
        this.tokenProperties = tokenProperties;
        this.userRepository = userRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.emailService = emailService;
        this.urlService = urlService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(AppUser appUser) {
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userRepository.save(appUser);

        ConfirmationToken confirmationToken = new ConfirmationToken(appUser);
        confirmationToken.setType(ConfirmationToken.Type.CONFIRM_EMAIL);

        emailService.send(appUser.getEmail(),
                "Complete Registration!",
                "To confirm your account, please click here : "
                        + urlService.getThisURL() + "confirmAccount?token=" + confirmationToken.getConfirmationToken() + "&tokenId=" + confirmationToken.getId());

        confirmationToken.setConfirmationToken(passwordEncoder.encode(confirmationToken.getConfirmationToken()));
        confirmationTokenRepository.save(confirmationToken);
    }

    @Transactional
    public void confirm(ConfirmationToken dbToken) {
        AppUser appUser = userRepository.findByEmailIgnoreCase(dbToken.getAppUser().getEmail());
        appUser.setEnabled(true);
        userRepository.save(appUser);
        dbToken.setUsed(true);
        confirmationTokenRepository.save(dbToken);
    }

    @Transactional
    public void forgotPassword(AppUser appUser) {
        ConfirmationToken confirmationToken = new ConfirmationToken(appUser);
        confirmationToken.setType(ConfirmationToken.Type.RESET_PASSWORD);
        emailService.send(appUser.getEmail(),
                "Complete Password Reset!",
                "To complete the password reset process, please click here: "
                        + urlService.getThisURL() + "resetPassword?token=" + confirmationToken.getConfirmationToken() + "&tokenId=" + confirmationToken.getId());
        confirmationToken.setConfirmationToken(passwordEncoder.encode(confirmationToken.getConfirmationToken()));
        confirmationTokenRepository.save(confirmationToken);
    }

    @Transactional
    public void resetPassword(ResetPassword appUser, ConfirmationToken dbToken) {
        AppUser tokenAppUser = userRepository.findByEmailIgnoreCase(appUser.getEmail());
        tokenAppUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userRepository.save(tokenAppUser);
        dbToken.setUsed(true);
        confirmationTokenRepository.save(dbToken);
    }

    public boolean isCorrectAccountConfirmToken(String token, ConfirmationToken dbToken) {
        return dbToken != null
                && Utils.hoursDiff(dbToken.getCreatedDate(), new Date()) <= tokenProperties.getAccountExpiration()
                && passwordEncoder.matches(token, dbToken.getConfirmationToken())
                && dbToken.getType() == ConfirmationToken.Type.CONFIRM_EMAIL
                && !dbToken.isUsed();
    }

    public boolean isCorrectResetPasswordToken(String token, ConfirmationToken dbToken) {
        return dbToken != null
                && Utils.hoursDiff(dbToken.getCreatedDate(), new Date()) <= tokenProperties.getPasswordExpiration()
                && passwordEncoder.matches(token, dbToken.getConfirmationToken())
                && dbToken.getType() == ConfirmationToken.Type.RESET_PASSWORD
                && !dbToken.isUsed();
    }
}
