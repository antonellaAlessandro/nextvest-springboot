package nextvest.service;

import nextvest.model.Usuario;
import nextvest.model.Wallet;
import nextvest.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(Usuario usuario) {
        Wallet wallet = new Wallet();
        wallet.setUsuario(usuario);
        wallet.setAvailableBalance(0.0);
        return walletRepository.save(wallet);
    }

    public Wallet getByUserId(Integer userId) {
        return walletRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public Wallet addBalance(Integer userId, Double amount) {
        Wallet wallet = getByUserId(userId);
        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        return walletRepository.save(wallet);
    }

    public Wallet deductBalance(Integer userId, Double amount) {
        Wallet wallet = getByUserId(userId);
        if (wallet.getAvailableBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance() - amount);
        return walletRepository.save(wallet);
    }
}