package com.example.lotto.controller;

import com.example.lotto.domain.RulePreset;
import com.example.lotto.model.GenerationRequest;
import com.example.lotto.model.GenerationResult;
import com.example.lotto.model.PresetRequest;
import com.example.lotto.model.WinningNumberUpdateRequest;
import com.example.lotto.service.AdminAuditLogService;
import com.example.lotto.service.GenerationHistoryService;
import com.example.lotto.service.LottoGenerationService;
import com.example.lotto.service.RulePresetService;
import com.example.lotto.service.StatsService;
import com.example.lotto.service.WinningNumberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
public class HomeController {

    private final LottoGenerationService lottoGenerationService;
    private final WinningNumberService winningNumberService;
    private final RulePresetService rulePresetService;
    private final GenerationHistoryService generationHistoryService;
    private final StatsService statsService;
    private final AdminAuditLogService adminAuditLogService;

    public HomeController(
            LottoGenerationService lottoGenerationService,
            WinningNumberService winningNumberService,
            RulePresetService rulePresetService,
            GenerationHistoryService generationHistoryService,
            StatsService statsService,
            AdminAuditLogService adminAuditLogService
    ) {
        this.lottoGenerationService = lottoGenerationService;
        this.winningNumberService = winningNumberService;
        this.rulePresetService = rulePresetService;
        this.generationHistoryService = generationHistoryService;
        this.statsService = statsService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("generationRequest", new GenerationRequest());
        addUserModel(model, principal);
        return "index";
    }

    @GetMapping("/generate")
    public String generatePage(Model model, Principal principal) {
        return index(model, principal);
    }

    @GetMapping("/presets/{id}/apply")
    public String applyPreset(@PathVariable Long id, Model model, Principal principal) {
        try {
            RulePreset preset = rulePresetService.findMine(id, principal.getName());
            GenerationRequest request = rulePresetService.toRequest(id, principal.getName());
            GenerationResult result = lottoGenerationService.generate(request);
            generationHistoryService.save(principal.getName(), request, result);
            model.addAttribute("generationRequest", request);
            model.addAttribute("result", result);
            model.addAttribute("selectedPresetName", preset.getName());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("generationRequest", new GenerationRequest());
            model.addAttribute("errorMessage", exception.getMessage());
        }
        addUserModel(model, principal);
        return "index";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute("generationRequest") GenerationRequest request, Model model, Principal principal) {
        try {
            GenerationResult result = lottoGenerationService.generate(request);
            generationHistoryService.save(principal.getName(), request, result);
            model.addAttribute("result", result);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        addUserModel(model, principal);
        return "index";
    }

    @PostMapping("/presets")
    public String savePreset(@ModelAttribute PresetRequest request, Model model, Principal principal) {
        try {
            rulePresetService.save(principal.getName(), request);
            model.addAttribute("presetMessage", "프리셋을 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("presetErrorMessage", exception.getMessage());
        }
        model.addAttribute("generationRequest", request);
        addUserModel(model, principal);
        return "index";
    }

    @PostMapping("/presets/{id}/delete")
    public String deletePreset(@PathVariable Long id, Model model, Principal principal) {
        try {
            rulePresetService.deleteMine(id, principal.getName());
            model.addAttribute("presetMessage", "프리셋을 삭제했습니다.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("presetErrorMessage", exception.getMessage());
        }
        model.addAttribute("generationRequest", new GenerationRequest());
        addUserModel(model, principal);
        return "index";
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("stats", statsService.summarize());
        return "stats";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("winningNumberUpdateRequest", new WinningNumberUpdateRequest());
        addAdminModel(model);
        return "admin";
    }

    @PostMapping("/admin/winning-numbers")
    public String updateWinningNumbers(
            @ModelAttribute("winningNumberUpdateRequest") WinningNumberUpdateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            winningNumberService.saveOrUpdate(request);
            adminAuditLogService.record(principal.getName(), "MANUAL_SAVE", request.getDrawNumber() + "회차 당첨번호 수동 저장");
            model.addAttribute("winningNumberMessage", request.getDrawNumber() + "회차 당첨번호를 저장했습니다.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("winningNumberErrorMessage", exception.getMessage());
        }
        addAdminModel(model);
        return "admin";
    }

    @PostMapping("/admin/winning-numbers/upload")
    public String uploadWinningNumbers(MultipartFile file, Model model, Principal principal) {
        try {
            int importedCount = winningNumberService.importCsv(file);
            adminAuditLogService.record(principal.getName(), "CSV_UPLOAD", importedCount + "개 회차 CSV 업로드");
            model.addAttribute("winningNumberMessage", importedCount + "개 회차를 업로드했습니다.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("winningNumberErrorMessage", exception.getMessage());
        }
        model.addAttribute("winningNumberUpdateRequest", new WinningNumberUpdateRequest());
        addAdminModel(model);
        return "admin";
    }

    @PostMapping("/admin/winning-numbers/external-update")
    public String updateFromExternalApi(Model model, Principal principal) {
        int updatedCount = winningNumberService.updateFromExternalApi();
        adminAuditLogService.record(principal.getName(), "EXTERNAL_UPDATE", updatedCount + "개 회차 외부 API 업데이트");
        model.addAttribute("winningNumberMessage", updatedCount + "개 회차를 외부 API에서 업데이트했습니다.");
        model.addAttribute("winningNumberUpdateRequest", new WinningNumberUpdateRequest());
        addAdminModel(model);
        return "admin";
    }

    private void addUserModel(Model model, Principal principal) {
        addWinningNumberSummary(model);
        model.addAttribute("username", principal.getName());
        model.addAttribute("isAdmin", isAdmin(principal));
        model.addAttribute("presets", rulePresetService.findMine(principal.getName()));
        model.addAttribute("histories", generationHistoryService.findMine(principal.getName()));
    }

    private boolean isAdmin(Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void addAdminModel(Model model) {
        addWinningNumberSummary(model);
        model.addAttribute("latestWinningDraws", winningNumberService.findLatest(10));
        model.addAttribute("auditLogs", adminAuditLogService.findRecent());
    }

    private void addWinningNumberSummary(Model model) {
        model.addAttribute("winningDrawCount", winningNumberService.count());
        model.addAttribute("latestWinningDraw", winningNumberService.findLatestDraw().orElse(null));
    }
}
