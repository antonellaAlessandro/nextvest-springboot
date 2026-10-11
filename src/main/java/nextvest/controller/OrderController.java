package nextvest.controller;

import nextvest.model.Order;
import nextvest.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/buy")
    public ResponseEntity<Order> buy(@RequestBody BuyRequest request) {
        try {
            Order order = orderService.buy(request.getUserId(),
                    request.getInstrumentId(), request.getUnits());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<Order> sell(@RequestBody SellRequest request) {
     try {
        Order order = orderService.sell(request.getUserId(),
                request.getInstrumentId(), request.getUnits());
        return ResponseEntity.ok(order);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().build();
    }
}

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getHistory(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Order>> getHistoryByType(@PathVariable Integer userId,
                                                         @PathVariable String type) {
        return ResponseEntity.ok(orderService.getHistoryByType(userId, type));
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