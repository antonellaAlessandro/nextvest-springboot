package nextvest.controller;

import nextvest.dto.PortfolioResponse;
import nextvest.dto.PositionResponse;
import nextvest.model.User;
import nextvest.security.AccessGuard;
import nextvest.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final AccessGuard accessGuard;

    public PortfolioController(PortfolioService portfolioService, AccessGuard accessGuard) {
        this.portfolioService = portfolioService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioResponse> getByUserId(@PathVariable Integer userId,
                                                         Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(portfolioService.toResponse(
                portfolioService.getByUserId(userId)));
    }

    @GetMapping("/user/{userId}/positions")
    public ResponseEntity<List<PositionResponse>> getPositions(@PathVariable Integer userId,
                                                               Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(portfolioService.toPositionResponseList(
                portfolioService.getPositions(userId)));
    }

    @PostMapping("/user/{userId}/create")
    public ResponseEntity<PortfolioResponse> createPortfolio(@PathVariable Integer userId,
                                                             Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(portfolioService.toResponse(
                portfolioService.createPortfolio(user)));
    }
}