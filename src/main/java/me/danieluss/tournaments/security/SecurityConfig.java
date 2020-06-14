package me.danieluss.tournaments.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/scripts/**",
                        "/styles/**",
                        "/webjars/**",
                        "/favicon.ico").permitAll()
                .antMatchers(
                        "/",
                        "/\\?*",
                        "/home*",
                        "/list*",
                        "/register",
                        "/login**",
                        "/logout",
                        "/confirmAccount**",
                        "/forgotPassword**",
                        "/resetPassword**",
                        "/images/*/*",
                        "/doResetPassword**",
                        "/details**").permitAll()
        .and().authorizeRequests()
                .antMatchers("/**").fullyAuthenticated()
        .and().formLogin()
                .loginPage("/login")
                .permitAll()
        .and().logout()
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        .and()
                .sessionManagement()
                .maximumSessions(1);
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }
}
