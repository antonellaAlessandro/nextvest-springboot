package nextvest.service;

import nextvest.exception.BusinessException;
import nextvest.model.Instrument;
import nextvest.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Actualiza el precio de los instrumentos activos con los datos de IOL.
 * Si IOL no responde o no tiene el instrumento, se conserva el último precio guardado.
 */
@Service
public class PriceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateService.class);

    private final InstrumentRepository instrumentRepository;
    private final IolClient iolClient;
    private final boolean enabled;

    public PriceUpdateService(InstrumentRepository instrumentRepository,
                              IolClient iolClient,
                              @Value("${iol.enabled:false}") boolean enabled) {
        this.instrumentRepository = instrumentRepository;
        this.iolClient = iolClient;
        this.enabled = enabled;
    }

    // Cada 15 minutos, de lunes a viernes, en horario de mercado.
    @Scheduled(cron = "0 */15 11-17 * * MON-FRI", zone = "America/Argentina/Buenos_Aires")
    public void scheduledUpdate() {
        if (enabled && iolClient.isConfigured()) {
            int updated = updateAll();
            log.info("Precios actualizados desde IOL: {}", updated);
        }
    }

    // Devuelve la cantidad de instrumentos cuyo precio se actualizó.
    public int updateAll() {
        if (!iolClient.isConfigured()) {
            throw new BusinessException("La conexión con IOL no está configurada");
        }

        int updated = 0;
        for (Instrument instrument : instrumentRepository.findByActiveTrue()) {
            try {
                // Los FCI se consultan con plazo T0; el resto con T1.
                Optional<Double> price = "FCI".equals(instrument.getType())
                        ? iolClient.getFciPrice(instrument.getTicker())
                        : iolClient.getMarketPrice(instrument.getTicker());

                if (price.isPresent()) {
                    instrument.setCurrentPrice(price.get());
                    instrumentRepository.save(instrument);
                    updated++;
                } else {
                    log.warn("IOL no devolvió precio para {}", instrument.getTicker());
                }
            } catch (Exception e) {
                log.warn("No se pudo actualizar {}: {}", instrument.getTicker(), e.getMessage());
            }
        }
        return updated;
    }
}