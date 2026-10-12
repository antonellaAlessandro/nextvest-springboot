package nextvest.service;

import nextvest.model.Portfolio;
import nextvest.model.Position;
import nextvest.model.Usuario;
import nextvest.repository.PortfolioRepository;
import nextvest.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import nextvest.dto.PositionResponse;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            PositionRepository positionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
    }

    public Portfolio createPortfolio(Usuario usuario) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUsuario(usuario);
        portfolio.setTotalValue(0.0);
        return portfolioRepository.save(portfolio);
    }

    public Portfolio getByUserId(Integer userId) {
        return portfolioRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));
    }

    public List<Position> getPositions(Integer userId) {
        Portfolio portfolio = getByUserId(userId);
        return positionRepository.findByPortfolioId(portfolio.getId());
    }

    public Position addOrUpdatePosition(Integer portfolioId, Integer instrumentId,
                                        Integer units, Double price) {
        var existingPosition = positionRepository
                .findByPortfolioIdAndInstrumentId(portfolioId, instrumentId);

        if (existingPosition.isPresent()) {
            Position position = existingPosition.get();
            int totalUnits = position.getUnits() + units;
            double newAverage = ((position.getAverageBuyPrice() * position.getUnits())
                    + (price * units)) / totalUnits;
            position.setUnits(totalUnits);
            position.setAverageBuyPrice(newAverage);
            return positionRepository.save(position);
        } else {
            Position position = new Position();
            Portfolio portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));
            position.setPortfolio(portfolio);
            nextvest.model.Instrument instrument = new nextvest.model.Instrument();
            instrument.setId(instrumentId);
            position.setInstrument(instrument);
            position.setUnits(units);
            position.setAverageBuyPrice(price);
            return positionRepository.save(position);
        }
    }

    public void updateTotalValue(Integer userId, Double totalValue) {
        Portfolio portfolio = getByUserId(userId);
        portfolio.setTotalValue(totalValue);
        portfolioRepository.save(portfolio);
    }

    public PositionResponse toPositionResponse(Position position) {
        double currentValue = position.getUnits() * position.getInstrument().getCurrentPrice();
        double gainLoss = currentValue - (position.getUnits() * position.getAverageBuyPrice());

        return new PositionResponse(
                position.getId(),
                position.getUnits(),
                position.getAverageBuyPrice(),
                position.getInstrument().getName(),
                position.getInstrument().getTicker(),
                position.getInstrument().getCurrentPrice(),
                currentValue,
                gainLoss
        );
    }

    public List<PositionResponse> toPositionResponseList(List<Position> positions) {
        return positions.stream()
                .map(this::toPositionResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}