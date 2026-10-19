package nextvest.controller;

import nextvest.model.AuthorizationToken;
import nextvest.model.LegalRepresentative;
import nextvest.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/user/{userId}/representative")
    public ResponseEntity<LegalRepresentative> registerRepresentative(
            @PathVariable Integer userId,
            @RequestBody LegalRepresentative representative) {
        try {
            return ResponseEntity.ok(
                    authorizationService.registerRepresentative(userId, representative));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{userId}/token")
    public ResponseEntity<AuthorizationToken> generateToken(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(authorizationService.generateToken(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/authorize/{uuid}")
    public ResponseEntity<Void> authorize(@PathVariable String uuid) {
        try {
            authorizationService.authorize(uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/reject/{uuid}")
    public ResponseEntity<Void> reject(@PathVariable String uuid) {
        try {
            authorizationService.reject(uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/representative")
    public ResponseEntity<LegalRepresentative> getRepresentative(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(
                    authorizationService.getRepresentativeByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}