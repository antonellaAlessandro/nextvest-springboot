package nextvest.controller;

import nextvest.model.Instrument;
import nextvest.model.RiskProfile;
import nextvest.service.InstrumentService;
import nextvest.service.RiskProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final RiskProfileService riskProfileService;

    public InstrumentController(InstrumentService instrumentService,
                                 RiskProfileService riskProfileService) {
        this.instrumentService = instrumentService;
        this.riskProfileService = riskProfileService;
    }

    @GetMapping
    public ResponseEntity<List<Instrument>> getAll() {
        return ResponseEntity.ok(instrumentService.getAllActive());
    }

    @GetMapping("/by-profile/{profile}")
    public ResponseEntity<List<Instrument>> getByProfile(@PathVariable nextvest.model.RiskProfileType profile) {
        return ResponseEntity.ok(instrumentService.getByRiskProfile(profile));
    }

    @GetMapping("/market")
    public ResponseEntity<List<Instrument>> getMarket() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();

        try {
            var profile = riskProfileService.getByEmail(email);
            return ResponseEntity.ok(instrumentService.getByRiskProfile(profile.getType()));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(instrumentService.getAllActive());
        }
    }

    @PostMapping
    public ResponseEntity<Instrument> create(@RequestBody Instrument instrument) {
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
}