package nextvest.controller;

import nextvest.model.RiskProfile;
import nextvest.service.RiskProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-profiles")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @PostMapping("/user/{userId}/evaluate")
    public ResponseEntity<RiskProfile> evaluate(@PathVariable Integer userId,
                                                 @RequestBody List<String> answers) {
        try {
            return ResponseEntity.ok(riskProfileService.evaluate(userId, answers));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RiskProfile> getByUserId(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(riskProfileService.getByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}