package nextvest.controller;

import nextvest.dto.OrderResponse;
import nextvest.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/buy")
    public ResponseEntity<OrderResponse> buy(@RequestBody BuyRequest request) {
        try {
            return ResponseEntity.ok(orderService.toResponse(
                    orderService.buy(request.getUserId(),
                            request.getInstrumentId(), request.getUnits())));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<OrderResponse> sell(@RequestBody SellRequest request) {
        try {
            return ResponseEntity.ok(orderService.toResponse(
                    orderService.sell(request.getUserId(),
                            request.getInstrumentId(), request.getUnits())));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getHistory(userId)
                .stream()
                .map(orderService::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<OrderResponse>> getHistoryByType(@PathVariable Integer userId,
                                                                  @PathVariable String type) {
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