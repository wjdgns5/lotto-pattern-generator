package com.example.lotto.service;

import com.example.lotto.domain.AdminAuditLog;
import com.example.lotto.repository.AdminAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    public AdminAuditLogService(AdminAuditLogRepository adminAuditLogRepository) {
        this.adminAuditLogRepository = adminAuditLogRepository;
    }

    @Transactional
    public void record(String username, String actionType, String detail) {
        adminAuditLogRepository.save(new AdminAuditLog(username, actionType, detail));
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLog> findRecent() {
        return adminAuditLogRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
