package com.example.lotto.service;

import com.example.lotto.domain.UserAccount;
import com.example.lotto.domain.UserRole;
import com.example.lotto.model.RegisterRequest;
import com.example.lotto.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void createDefaultAdmin() {
        // 개발/시연용 기본 관리자 계정입니다. 이미 존재하면 중복 생성하지 않습니다.
        if (!userAccountRepository.existsByUsername("admin")) {
            userAccountRepository.save(new UserAccount(
                    "admin",
                    passwordEncoder.encode("admin1234"),
                    UserRole.ADMIN
            ));
        }
    }

    @Transactional
    public void register(RegisterRequest request) {
        // 회원가입 요청값을 검증한 뒤 USER 권한 계정을 생성합니다.
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.length() < 4 || username.length() > 30) {
            throw new IllegalArgumentException("아이디는 4~30자로 입력해 주세요.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
        if (userAccountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        userAccountRepository.save(new UserAccount(
                username,
                passwordEncoder.encode(request.getPassword()),
                UserRole.USER
        ));
    }
}
