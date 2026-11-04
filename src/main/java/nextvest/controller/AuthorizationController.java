package nextvest.controller;

import nextvest.dto.AuthorizationTokenResponse;
import nextvest.dto.LegalRepresentativeResponse;
import nextvest.model.LegalRepresentative;
import nextvest.security.AccessGuard;
import nextvest.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;
    private final AccessGuard accessGuard;

    public AuthorizationController(AuthorizationService authorizationService,
                                   AccessGuard accessGuard) {
        this.authorizationService = authorizationService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/user/{userId}/representative")
    public ResponseEntity<LegalRepresentativeResponse> registerRepresentative(
            @PathVariable Integer userId,
            @RequestBody LegalRepresentative representative,
            Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(authorizationService.toRepresentativeResponse(
                authorizationService.registerRepresentative(userId, representative)));
    }

    @PostMapping("/user/{userId}/token")
    public ResponseEntity<AuthorizationTokenResponse> generateToken(@PathVariable Integer userId) {
        return ResponseEntity.ok(authorizationService.toTokenResponse(
                authorizationService.generateToken(userId)));
    }

    @PutMapping("/authorize/{uuid}")
    public ResponseEntity<Void> authorize(@PathVariable String uuid) {
        authorizationService.authorize(uuid);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reject/{uuid}")
    public ResponseEntity<Void> reject(@PathVariable String uuid) {
        authorizationService.reject(uuid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/representative")
    public ResponseEntity<LegalRepresentativeResponse> getRepresentative(
            @PathVariable Integer userId,
            Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(authorizationService.toRepresentativeResponse(
                authorizationService.getRepresentativeByUserId(userId)));
    }
}