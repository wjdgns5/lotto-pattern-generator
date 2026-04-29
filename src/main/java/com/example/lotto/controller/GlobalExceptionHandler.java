package com.example.lotto.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException exception, Model model, Principal principal) {
        addErrorModel(model, 400, "잘못된 요청입니다", exception.getMessage(), principal);
        return "common/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model, Principal principal) {
        addErrorModel(model, 403, "접근 권한이 없습니다", "현재 계정으로는 이 기능을 사용할 수 없습니다.", principal);
        return "common/error";
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model, Principal principal) {
        addErrorModel(model, 404, "페이지를 찾을 수 없습니다", "요청한 페이지가 없거나 주소가 변경되었습니다.", principal);
        return "common/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleServerError(Model model, Principal principal) {
        addErrorModel(model, 500, "요청을 처리하지 못했습니다", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", principal);
        return "common/error";
    }

    private void addErrorModel(Model model, int status, String title, String message, Principal principal) {
        model.addAttribute("status", status);
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        model.addAttribute("returnUrl", principal == null ? "/login" : "/generate");
        model.addAttribute("returnText", principal == null ? "로그인으로 이동" : "사용자 화면으로 이동");
    }
}
