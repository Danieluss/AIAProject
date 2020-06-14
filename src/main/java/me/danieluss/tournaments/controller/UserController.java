package me.danieluss.tournaments.controller;

import me.danieluss.tournaments.data.dto.RegisterUser;
import me.danieluss.tournaments.data.dto.ResetPassword;
import me.danieluss.tournaments.data.model.AppUser;
import me.danieluss.tournaments.data.model.ConfirmationToken;
import me.danieluss.tournaments.data.repo.ConfirmationTokenRepository;
import me.danieluss.tournaments.data.repo.UserRepository;
import me.danieluss.tournaments.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.UUID;

@Controller
public class UserController {

    private final UserRepository userRepository;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, ConfirmationTokenRepository confirmationTokenRepository, UserService userService) {
        this.userRepository = userRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.userService = userService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Model model) {
        if (!model.containsAttribute("registerUser"))
            model.addAttribute("registerUser", new RegisterUser());
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerUser(Model model, RedirectAttributes redirectAttrs, @Valid RegisterUser registerUser, BindingResult bindingResult) {
        if (!registerUser.getConfirmPassword().equals(registerUser.getPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerUser", "Passwords do not match!");
        }
        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.registerUser", bindingResult);
            redirectAttrs.addFlashAttribute("registerUser", registerUser);
            return "redirect:register";
        }

        AppUser appUser = new AppUser(registerUser);

        AppUser existingAppUser = userRepository.findByEmailIgnoreCase(appUser.getEmail());
        if (existingAppUser != null) {
            bindingResult.rejectValue("email", "error.registerUser", "This email already exists!");
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.registerUser", bindingResult);
            redirectAttrs.addFlashAttribute("registerUser", registerUser);
            return "redirect:register";
        } else {
            userService.register(appUser);

            redirectAttrs.addFlashAttribute("toastMsg", String.format("Confirmation email has been sent to %s!", appUser.getEmail()));
            return "redirect:home";
        }
    }

    @RequestMapping(value = "/confirmAccount", method = RequestMethod.GET)
    public String confirm(Model model, RedirectAttributes redirectAttrs, @RequestParam("tokenId") String tokenId, @RequestParam("token") String token) {
        ConfirmationToken dbToken = confirmationTokenRepository.findById(UUID.fromString(tokenId)).orElse(null);
        if (userService.isCorrectAccountConfirmToken(token, dbToken)) {
            userService.confirm(dbToken);

            redirectAttrs.addFlashAttribute("toastMsg", "You can sign in now.");
            return "redirect:home";
        } else {
            redirectAttrs.addFlashAttribute("toastMsg", "The link is invalid!");
            return "redirect:home";
        }
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
    public String forgot(Model model, AppUser appUser) {
        model.addAttribute("user", appUser);
        return "forgotPassword";
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public String forgotUser(Model model, RedirectAttributes redirectAttrs, AppUser appUser) {
        AppUser dbAppUser = userRepository.findByEmailIgnoreCase(appUser.getEmail());
        if (dbAppUser != null) {
            userService.forgotPassword(dbAppUser);
        }
        redirectAttrs.addFlashAttribute("toastMsg", String.format("Check your email %s.", appUser.getEmail()));
        return "redirect:home";
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public String reset(Model model, RedirectAttributes redirectAttrs, @RequestParam("token") String token, @RequestParam("tokenId") String tokenId) {
        ConfirmationToken dbToken = confirmationTokenRepository.findById(UUID.fromString(tokenId)).orElse(null);
        if (userService.isCorrectResetPasswordToken(token, dbToken)) {
            ResetPassword registerUser = new ResetPassword();
            if (!model.containsAttribute("user")) {
                model.addAttribute("user", registerUser);
            }
            model.addAttribute("token", token);
            model.addAttribute("tokenId", tokenId);
            return "resetPassword";
        } else {
            redirectAttrs.addFlashAttribute("toastMsg", "The link is invalid!");
            return "redirect:home";
        }
    }

    @RequestMapping(value = "/doResetPassword", method = RequestMethod.POST)
    public String resetUser(Model model, RedirectAttributes redirectAttrs, @Valid ResetPassword registerUser, BindingResult bindingResult, @NotBlank String token, @NotBlank String tokenId) {
        model.addAttribute("token", token);
        model.addAttribute("tokenId", tokenId);
        if (!registerUser.getConfirmPassword().equals(registerUser.getPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match!");
        }
        ConfirmationToken dbToken = confirmationTokenRepository.findById(UUID.fromString(Optional.of(tokenId).orElse(""))).orElse(null);
        if ((dbToken != null
                && registerUser.getEmail() != null
                && !registerUser.getEmail().equals(dbToken.getAppUser().getEmail()))) {
            bindingResult.rejectValue("email", "error.user", "Provide associated email.");
        }
        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttrs.addFlashAttribute("user", registerUser);
            return "redirect:resetPassword?token=" + token + "&tokenId=" + tokenId;
        }
        if (userService.isCorrectResetPasswordToken(token, dbToken)) {
            userService.resetPassword(registerUser, dbToken);

            redirectAttrs.addFlashAttribute("toastMsg", "Password successfully reset. You can now log in with the new credentials.");
            return "redirect:home";
        } else {
            redirectAttrs.addFlashAttribute("toastMsg", "Invalid token.");
            return "redirect:home";
        }
    }
}
