package nextvest.repository;

import nextvest.model.Instrument;
import nextvest.model.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {

    List<Instrument> findByActiveTrue();
    List<Instrument> findByActiveTrueAndMinimumProfileLessThanEqual(RiskProfile minimumProfile);
}