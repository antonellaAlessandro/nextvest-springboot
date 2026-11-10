package nextvest.service;

import nextvest.exception.BusinessException;
import nextvest.exception.NotFoundException;
import nextvest.model.Instrument;
import nextvest.model.RiskProfileType;
import nextvest.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class InstrumentService {

    // Tipos de instrumento que maneja la plataforma.
    private static final List<String> ALLOWED_TYPES = List.of("FCI", "CEDEAR", "BOND", "STOCK");

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<Instrument> getAllActive() {
        return instrumentRepository.findByActiveTrue();
    }

    // Devuelve los instrumentos cuyo perfil mínimo es igual o menor al del usuario.
    // Depende de que el enum RiskProfileType esté ordenado de menor a mayor riesgo.
    public List<Instrument> getByRiskProfile(RiskProfileType profile) {
        List<RiskProfileType> allowed = Arrays.stream(RiskProfileType.values())
                .filter(p -> p.ordinal() <= profile.ordinal())
                .toList();
        return instrumentRepository.findByActiveTrueAndMinimumProfileIn(allowed);
    }

    public Instrument save(Instrument instrument) {
        if (isBlank(instrument.getName())
                || isBlank(instrument.getTicker())
                || isBlank(instrument.getType())
                || instrument.getMinimumProfile() == null) {
            throw new BusinessException("Faltan datos obligatorios del instrumento");
        }

        String type = instrument.getType().trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException(
                    "El tipo de instrumento no es válido. Usá FCI, CEDEAR, BOND o STOCK");
        }
        instrument.setType(type);

        validatePrice(instrument.getCurrentPrice());
        if (instrument.getActive() == null) {
            instrument.setActive(true);
        }
        return instrumentRepository.save(instrument);
    }

    public void toggleActive(Integer id) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrumento no encontrado"));
        instrument.setActive(!instrument.getActive());
        instrumentRepository.save(instrument);
    }

    public void updatePrice(Integer id, Double price) {
        validatePrice(price);
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrumento no encontrado"));
        instrument.setCurrentPrice(price);
        instrumentRepository.save(instrument);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validatePrice(Double price) {
        if (price == null || !Double.isFinite(price) || price <= 0) {
            throw new BusinessException("El precio debe ser mayor a cero");
        }
    }
}