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

    // Los errores de validación y de "no encontrado" los resuelve el manejo
    // centralizado de excepciones, que devuelve el mensaje en el campo detail.
    @PostMapping("/user/{userId}/evaluate")
    public ResponseEntity<RiskProfileResponse> evaluate(@PathVariable Integer userId,
                                                        @RequestBody List<String> answers,
                                                        Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(riskProfileService.toResponse(
                riskProfileService.evaluate(userId, answers)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RiskProfileResponse> getByUserId(@PathVariable Integer userId,
                                                           Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(riskProfileService.toResponse(
                riskProfileService.getByUserId(userId)));
    }
}