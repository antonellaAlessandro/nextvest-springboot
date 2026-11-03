package nextvest.controller;

import nextvest.dto.WalletResponse;
import nextvest.model.User;
import nextvest.security.AccessGuard;
import nextvest.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final AccessGuard accessGuard;

    public WalletController(WalletService walletService, AccessGuard accessGuard) {
        this.walletService = walletService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getByUserId(@PathVariable Integer userId,
                                                      Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(walletService.toResponse(walletService.getByUserId(userId)));
    }

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<WalletResponse> addBalance(@PathVariable Integer userId,
                                                     @RequestBody Double amount,
                                                     Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(walletService.toResponse(walletService.addBalance(userId, amount)));
    }

    @PostMapping("/user/{userId}/deduct")
    public ResponseEntity<WalletResponse> deductBalance(@PathVariable Integer userId,
                                                        @RequestBody Double amount,
                                                        Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        try {
            return ResponseEntity.ok(walletService.toResponse(walletService.deductBalance(userId, amount)));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{userId}/create")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable Integer userId,
                                                       Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(walletService.toResponse(walletService.createWallet(user)));
    }
}