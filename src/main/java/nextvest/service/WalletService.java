package nextvest.service;

import nextvest.dto.WalletResponse;
import nextvest.model.User;
import nextvest.model.Wallet;
import nextvest.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAvailableBalance(0.0);
        return walletRepository.save(wallet);
    }

    public Wallet getByUserId(Integer userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Billetera no encontrada"));
    }

    public Wallet addBalance(Integer userId, Double amount) {
        Wallet wallet = getByUserId(userId);
        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        return walletRepository.save(wallet);
    }

    public Wallet deductBalance(Integer userId, Double amount) {
        Wallet wallet = getByUserId(userId);
        if (wallet.getAvailableBalance() < amount) {
            throw new RuntimeException("Saldo insuficiente");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance() - amount);
        return walletRepository.save(wallet);
    }

    public WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getAvailableBalance(),
                wallet.getUser().getId()
        );
    }
}