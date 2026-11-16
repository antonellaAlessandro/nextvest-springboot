package nextvest.controller;

import nextvest.exception.NotFoundException;
import nextvest.model.Instrument;
import nextvest.model.RiskProfileType;
import nextvest.service.InstrumentService;
import nextvest.service.PriceUpdateService;
import nextvest.service.RiskProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final RiskProfileService riskProfileService;
    private final PriceUpdateService priceUpdateService;

    public InstrumentController(InstrumentService instrumentService,
                                RiskProfileService riskProfileService,
                                PriceUpdateService priceUpdateService) {
        this.instrumentService = instrumentService;
        this.riskProfileService = riskProfileService;
        this.priceUpdateService = priceUpdateService;
    }

    @GetMapping
    public ResponseEntity<List<Instrument>> getAll() {
        return ResponseEntity.ok(instrumentService.getAllActive());
    }

    @GetMapping("/by-profile/{profile}")
    public ResponseEntity<List<Instrument>> getByProfile(@PathVariable RiskProfileType profile) {
        return ResponseEntity.ok(instrumentService.getByRiskProfile(profile));
    }

    @GetMapping("/market")
    public ResponseEntity<List<Instrument>> getMarket() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();

        try {
            var profile = riskProfileService.getByEmail(email);
            return ResponseEntity.ok(instrumentService.getByRiskProfile(profile.getType()));
        } catch (NotFoundException e) {
            // Sin test de perfil: solo se muestran los instrumentos de menor riesgo
            return ResponseEntity.ok(
                    instrumentService.getByRiskProfile(RiskProfileType.CONSERVATIVE));
        }
    }

    @PostMapping
    public ResponseEntity<Instrument> create(@RequestBody Instrument instrument) {
        instrument.setId(null);
        return ResponseEntity.ok(instrumentService.save(instrument));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
        instrumentService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Void> updatePrice(@PathVariable Integer id, @RequestBody Double price) {
        instrumentService.updatePrice(id, price);
        return ResponseEntity.ok().build();
    }

    // Pide a IOL los precios de todos los instrumentos activos.
    @PostMapping("/refresh-prices")
    public ResponseEntity<Map<String, Integer>> refreshPrices() {
        int updated = priceUpdateService.updateAll();
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}