package nextvest.repository;

import nextvest.model.Instrument;
import nextvest.model.RiskProfileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {

    List<Instrument> findByActiveTrue();

    List<Instrument> findByActiveTrueAndMinimumProfileIn(List<RiskProfileType> profiles);

    // Solo instrumentos que ya tienen precio (los nuevos esperan su primera actualización de IOL).
    List<Instrument> findByActiveTrueAndCurrentPriceNotNull();

    List<Instrument> findByActiveTrueAndCurrentPriceNotNullAndMinimumProfileIn(
            List<RiskProfileType> profiles);
}