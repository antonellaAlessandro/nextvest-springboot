package nextvest.controller;

import nextvest.dto.PositionResponse;
import nextvest.model.Portfolio;
import nextvest.model.User;
import nextvest.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Portfolio> getByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(portfolioService.getByUserId(userId));
    }

    @GetMapping("/user/{userId}/positions")
    public ResponseEntity<List<PositionResponse>> getPositions(@PathVariable Integer userId) {
        return ResponseEntity.ok(portfolioService.toPositionResponseList(
                portfolioService.getPositions(userId)));
    }

    @PostMapping("/user/{userId}/create")
    public ResponseEntity<Portfolio> createPortfolio(@PathVariable Integer userId) {
        User user = new User();
        user.setId(userId);
        return ResponseEntity.ok(portfolioService.createPortfolio(user));
    }
}