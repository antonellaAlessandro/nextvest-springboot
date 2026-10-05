package nextvest.service;

import nextvest.model.Instrument;
import nextvest.model.RiskProfile;
import nextvest.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<Instrument> getAllActive() {
        return instrumentRepository.findByActiveTrue();
    }

    public List<Instrument> getByRiskProfile(RiskProfile profile) {
        return instrumentRepository.findByActiveTrueAndMinimumProfileLessThanEqual(profile);
    }

    public Instrument save(Instrument instrument) {
        return instrumentRepository.save(instrument);
    }

    public void toggleActive(Integer id) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));
        instrument.setActive(!instrument.getActive());
        instrumentRepository.save(instrument);
    }

    public void updatePrice(Integer id, Double price) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));
        instrument.setCurrentPrice(price);
        instrumentRepository.save(instrument);
    }
}