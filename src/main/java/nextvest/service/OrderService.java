package nextvest.service;

import nextvest.dto.OrderResponse;
import nextvest.exception.BusinessException;
import nextvest.exception.NotFoundException;
import nextvest.model.Instrument;
import nextvest.model.Order;
import nextvest.model.Portfolio;
import nextvest.model.Position;
import nextvest.model.RiskProfile;
import nextvest.model.User;
import nextvest.repository.InstrumentRepository;
import nextvest.repository.OrderRepository;
import nextvest.repository.PositionRepository;
import nextvest.repository.RiskProfileRepository;
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
    private final RiskProfileRepository riskProfileRepository;

    public OrderService(OrderRepository orderRepository,
                        WalletService walletService,
                        PortfolioService portfolioService,
                        InstrumentRepository instrumentRepository,
                        PositionRepository positionRepository,
                        UserRepository userRepository,
                        RiskProfileRepository riskProfileRepository) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.portfolioService = portfolioService;
        this.instrumentRepository = instrumentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.riskProfileRepository = riskProfileRepository;
    }

    @Transactional
    public Order buy(Integer userId, Integer instrumentId, Integer units) {
        validateUnits(units);
        User user = getActiveUser(userId);

        Instrument instrument = findInstrument(instrumentId);
        if (!Boolean.TRUE.equals(instrument.getActive())) {
            throw new BusinessException("Este instrumento no está disponible para operar");
        }
        requirePrice(instrument);
        requireSuitableProfile(userId, instrument);

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

        Instrument instrument = findInstrument(instrumentId);
        requirePrice(instrument);

        Portfolio portfolio = portfolioService.getByUserId(userId);

        Position position = positionRepository
                .findByPortfolioIdAndInstrumentId(portfolio.getId(), instrumentId)
                .orElseThrow(() -> new BusinessException(
                        "No tenés este instrumento en el portafolio"));

        if (position.getUnits() < units) {
            throw new BusinessException("No tenés suficientes unidades para vender");
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

    private Instrument findInstrument(Integer instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrumento no encontrado"));
    }

    private void validateUnits(Integer units) {
        if (units == null || units <= 0) {
            throw new BusinessException("La cantidad de unidades debe ser mayor a cero");
        }
    }

    private void requirePrice(Instrument instrument) {
        if (instrument.getCurrentPrice() == null || instrument.getCurrentPrice() <= 0) {
            throw new BusinessException("El instrumento no tiene un precio disponible");
        }
    }

    private void requireSuitableProfile(Integer userId, Instrument instrument) {
        RiskProfile profile = riskProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Completá el test de perfil de riesgo antes de operar"));

        if (instrument.getMinimumProfile().ordinal() > profile.getType().ordinal()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este instrumento no es compatible con tu perfil de riesgo");
        }
    }

    private User getActiveUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (!"ACTIVA".equals(user.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta todavía no está activa para operar");
        }
        return user;
    }
}