package nextvest.service;

import nextvest.dto.OrderResponse;
import nextvest.model.Instrument;
import nextvest.model.Order;
import nextvest.model.Portfolio;
import nextvest.model.Position;
import nextvest.model.User;
import nextvest.repository.InstrumentRepository;
import nextvest.repository.OrderRepository;
import nextvest.repository.PositionRepository;
import nextvest.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final PortfolioService portfolioService;
    private final InstrumentRepository instrumentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        WalletService walletService,
                        PortfolioService portfolioService,
                        InstrumentRepository instrumentRepository,
                        PositionRepository positionRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.portfolioService = portfolioService;
        this.instrumentRepository = instrumentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order buy(Integer userId, Integer instrumentId, Integer units) {
        validateUnits(units);
        User user = getActiveUser(userId);

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrumento no encontrado"));

        double totalAmount = instrument.getCurrentPrice() * units;

        walletService.deductBalance(userId, totalAmount);

        Portfolio portfolio = portfolioService.getByUserId(userId);
        portfolioService.addOrUpdatePosition(portfolio.getId(), instrumentId,
                units, instrument.getCurrentPrice());

        Order order = new Order();
        order.setType("BUY");
        order.setUnits(units);
        order.setUnitPrice(instrument.getCurrentPrice());
        order.setTotalAmount(totalAmount);
        order.setDateTime(LocalDateTime.now());
        order.setUser(user);
        order.setInstrument(instrument);

        return orderRepository.save(order);
    }

    @Transactional
    public Order sell(Integer userId, Integer instrumentId, Integer units) {
        validateUnits(units);
        User user = getActiveUser(userId);

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrumento no encontrado"));

        Portfolio portfolio = portfolioService.getByUserId(userId);

        Position position = positionRepository
                .findByPortfolioIdAndInstrumentId(portfolio.getId(), instrumentId)
                .orElseThrow(() -> new RuntimeException("No tenés este instrumento en el portafolio"));

        if (position.getUnits() < units) {
            throw new RuntimeException("No tenés suficientes unidades para vender");
        }

        double totalAmount = instrument.getCurrentPrice() * units;

        walletService.addBalance(userId, totalAmount);

        if (position.getUnits().equals(units)) {
            positionRepository.delete(position);
        } else {
            position.setUnits(position.getUnits() - units);
            positionRepository.save(position);
        }

        Order order = new Order();
        order.setType("SELL");
        order.setUnits(units);
        order.setUnitPrice(instrument.getCurrentPrice());
        order.setTotalAmount(totalAmount);
        order.setDateTime(LocalDateTime.now());
        order.setUser(user);
        order.setInstrument(instrument);

        return orderRepository.save(order);
    }

    public List<Order> getHistory(Integer userId) {
        return orderRepository.findByUserIdOrderByDateTimeDesc(userId);
    }

    public List<Order> getHistoryByType(Integer userId, String type) {
        return orderRepository.findByUserIdAndTypeOrderByDateTimeDesc(userId, type);
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getType(),
                order.getUnits(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getDateTime(),
                order.getInstrument().getName(),
                order.getInstrument().getTicker()
        );
    }

    private void validateUnits(Integer units) {
        if (units == null || units <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad de unidades debe ser mayor a cero");
        }
    }

    private User getActiveUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!"ACTIVA".equals(user.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta todavía no está activa para operar");
        }
        return user;
    }
}