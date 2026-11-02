package nextvest.controller;

import nextvest.service.RiskProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nextvest.dto.RiskProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/api/risk-profiles")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @PostMapping("/user/{userId}/evaluate")
    public ResponseEntity<RiskProfileResponse> evaluate(@PathVariable Integer userId,
                                                        @RequestBody List<String> answers) {
        try {
            return ResponseEntity.ok(riskProfileService.toResponse(
                    riskProfileService.evaluate(userId, answers)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RiskProfileResponse> getByUserId(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(riskProfileService.toResponse(
                    riskProfileService.getByUserId(userId)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}