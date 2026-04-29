package com.example.lotto.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class ErrorPageController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode == null ? 500 : Integer.parseInt(statusCode.toString());
        addErrorModel(model, status, principal);
        return "common/error";
    }

    @GetMapping("/error-page")
    public String errorPage(@RequestParam(defaultValue = "500") int status, Model model, Principal principal) {
        addErrorModel(model, status, principal);
        return "common/error";
    }

    private void addErrorModel(Model model, int status, Principal principal) {
        model.addAttribute("status", status);
        model.addAttribute("title", title(status));
        model.addAttribute("message", message(status));
        model.addAttribute("returnUrl", principal == null ? "/login" : "/generate");
        model.addAttribute("returnText", principal == null ? "로그인으로 이동" : "사용자 화면으로 이동");
    }

    private String title(int status) {
        return switch (status) {
            case 400 -> "잘못된 요청입니다";
            case 403 -> "접근 권한이 없습니다";
            case 404 -> "페이지를 찾을 수 없습니다";
            default -> "요청을 처리하지 못했습니다";
        };
    }

    private String message(int status) {
        return switch (status) {
            case 400 -> "입력값이나 요청 주소를 다시 확인해 주세요.";
            case 403 -> "현재 계정으로는 이 기능을 사용할 수 없습니다.";
            case 404 -> "요청한 페이지가 없거나 주소가 변경되었습니다.";
            default -> "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        };
    }
}
