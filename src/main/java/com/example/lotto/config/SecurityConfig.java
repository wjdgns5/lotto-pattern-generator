package com.example.lotto.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 회원가입/기본 관리자 계정 생성 시 비밀번호를 그대로 저장하지 않고 BCrypt로 해시합니다.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 로그인, 회원가입, 오류 페이지, 정적 리소스는 인증 없이 접근할 수 있게 둡니다.
        RequestMatcher publicPaths = request -> {
            String path = request.getRequestURI();
            return path.equals("/login")
                    || path.equals("/register")
                    || path.equals("/error")
                    || path.equals("/error-page")
                    || path.startsWith("/css/")
                    || path.startsWith("/js/");
        };
        // 관리자 화면과 H2 콘솔은 ADMIN 권한만 접근할 수 있습니다.
        RequestMatcher adminPaths = request -> {
            String path = request.getRequestURI();
            return path.startsWith("/admin/") || path.equals("/admin") || path.startsWith("/h2-console/");
        };
        // URL 권한, 로그인/로그아웃, 403 오류 처리, H2 콘솔 예외 설정을 한 곳에서 관리합니다.
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(publicPaths).permitAll()
                        .requestMatchers(adminPaths).hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/generate", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/error-page?status=403"))
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}
