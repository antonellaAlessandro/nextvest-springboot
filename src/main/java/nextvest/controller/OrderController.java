package nextvest.controller;

import nextvest.dto.OrderResponse;
import nextvest.security.AccessGuard;
import nextvest.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AccessGuard accessGuard;

    public OrderController(OrderService orderService, AccessGuard accessGuard) {
        this.orderService = orderService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/buy")
    public ResponseEntity<OrderResponse> buy(@RequestBody BuyRequest request,
                                             Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(request.getUserId(), authentication);
        try {
            return ResponseEntity.ok(orderService.toResponse(
                    orderService.buy(request.getUserId(),
                            request.getInstrumentId(), request.getUnits())));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<OrderResponse> sell(@RequestBody SellRequest request,
                                              Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(request.getUserId(), authentication);
        try {
            return ResponseEntity.ok(orderService.toResponse(
                    orderService.sell(request.getUserId(),
                            request.getInstrumentId(), request.getUnits())));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getHistory(@PathVariable Integer userId,
                                                          Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(orderService.getHistory(userId)
                .stream()
                .map(orderService::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<OrderResponse>> getHistoryByType(@PathVariable Integer userId,
                                                                @PathVariable String type,
                                                                Authentication authentication) {
        accessGuard.checkOwnerOrAdmin(userId, authentication);
        return ResponseEntity.ok(orderService.getHistoryByType(userId, type)
                .stream()
                .map(orderService::toResponse)
                .collect(Collectors.toList()));
    }

    static class BuyRequest {
        private Integer userId;
        private Integer instrumentId;
        private Integer units;

        public Integer getUserId() { return userId; }
        public Integer getInstrumentId() { return instrumentId; }
        public Integer getUnits() { return units; }
    }

    static class SellRequest {
        private Integer userId;
        private Integer instrumentId;
        private Integer units;

        public Integer getUserId() { return userId; }
        public Integer getInstrumentId() { return instrumentId; }
        public Integer getUnits() { return units; }
    }
}