package nextvest.controller;

import nextvest.model.Instrument;
import nextvest.model.RiskProfile;
import nextvest.service.InstrumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public ResponseEntity<List<Instrument>> getAll() {
        return ResponseEntity.ok(instrumentService.getAllActive());
    }

    @GetMapping("/by-profile/{profile}")
    public ResponseEntity<List<Instrument>> getByProfile(@PathVariable RiskProfile profile) {
        return ResponseEntity.ok(instrumentService.getByRiskProfile(profile));
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