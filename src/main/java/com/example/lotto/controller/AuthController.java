package com.example.lotto.controller;

import com.example.lotto.model.RegisterRequest;
import com.example.lotto.service.UserAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/login")
    public String login() {
        // Spring Security가 실제 로그인 검증을 처리하고, 이 메서드는 로그인 화면만 보여줍니다.
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        // 빈 회원가입 요청 객체를 화면에 전달해 JSP form과 바인딩할 수 있게 합니다.
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {
        try {
            // 회원가입 검증과 비밀번호 암호화는 UserAccountService에서 처리합니다.
            userAccountService.register(request);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("registerRequest", request);
            model.addAttribute("errorMessage", exception.getMessage());
            return "register";
        }
    }
}
