package nextvest.controller;

import nextvest.dto.RiskProfileResponse;
import nextvest.security.AccessGuard;
import nextvest.service.RiskProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-profiles")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;
    private final AccessGuard accessGuard;

    public RiskProfileController(RiskProfileService riskProfileService, AccessGuard accessGuard) {
        this.riskProfileService = riskProfileService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/user/{userId}/evaluate")
    public ResponseEntity<RiskProfileResponse> evaluate(@PathVariable Integer userId,
                                                        @RequestBody List<String> answers,
                                                        Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        try {
            return ResponseEntity.ok(riskProfileService.toResponse(
                    riskProfileService.evaluate(userId, answers)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RiskProfileResponse> getByUserId(@PathVariable Integer userId,
                                                           Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        try {
            return ResponseEntity.ok(riskProfileService.toResponse(
                    riskProfileService.getByUserId(userId)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}