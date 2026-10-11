package nextvest.service;

import nextvest.model.*;
import nextvest.repository.InstrumentRepository;
import nextvest.repository.OrderRepository;
import org.springframework.stereotype.Service;
import nextvest.repository.PositionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final PortfolioService portfolioService;
    private final InstrumentRepository instrumentRepository;
    private final PositionRepository positionRepository;

    public OrderService(OrderRepository orderRepository,
                        WalletService walletService,
                        PortfolioService portfolioService,
                        InstrumentRepository instrumentRepository, 
                        PositionRepository positionRepository) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.portfolioService = portfolioService;
        this.instrumentRepository = instrumentRepository;
        this.positionRepository = positionRepository;
    }

    public Order buy(Integer userId, Integer instrumentId, Integer units) {
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
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        order.setUsuario(usuario);
        order.setInstrument(instrument);

        return orderRepository.save(order);
    }
    public Order sell(Integer userId, Integer instrumentId, Integer units) {
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
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        order.setUsuario(usuario);
        order.setInstrument(instrument);

        return orderRepository.save(order);
    }

    public List<Order> getHistory(Integer userId) {
        return orderRepository.findByUsuarioIdOrderByDateTimeDesc(userId);
    }

    public List<Order> getHistoryByType(Integer userId, String type) {
        return orderRepository.findByUsuarioIdAndTypeOrderByDateTimeDesc(userId, type);
    }
}