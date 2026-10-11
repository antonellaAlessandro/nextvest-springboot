package nextvest.service;

import nextvest.model.Portfolio;
import nextvest.model.Position;
import nextvest.model.Usuario;
import nextvest.repository.PortfolioRepository;
import nextvest.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
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
                    .orElseThrow(() -> new RuntimeException("Portfolio not found"));
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
}