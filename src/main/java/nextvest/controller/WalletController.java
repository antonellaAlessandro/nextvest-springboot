package nextvest.controller;

import nextvest.dto.WalletResponse;
import nextvest.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponse> getByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(walletService.toResponse(walletService.getByUserId(userId)));
    }

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<WalletResponse> addBalance(@PathVariable Integer userId,
                                                      @RequestBody Double amount) {
        return ResponseEntity.ok(walletService.toResponse(walletService.addBalance(userId, amount)));
    }

    @PostMapping("/user/{userId}/deduct")
    public ResponseEntity<WalletResponse> deductBalance(@PathVariable Integer userId,
                                                         @RequestBody Double amount) {
        try {
            return ResponseEntity.ok(walletService.toResponse(walletService.deductBalance(userId, amount)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{userId}/create")
    public ResponseEntity<WalletResponse> createWallet(@PathVariable Integer userId) {
        nextvest.model.Usuario usuario = new nextvest.model.Usuario();
        usuario.setId(userId);
        return ResponseEntity.ok(walletService.toResponse(walletService.createWallet(usuario)));
    }
}