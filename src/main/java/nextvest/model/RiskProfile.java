package nextvest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "risk_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskProfileType type;

    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;

    @ElementCollection
    @CollectionTable(name = "risk_profile_answers",
            joinColumns = @JoinColumn(name = "risk_profile_id"))
    @Column(name = "answer")
    private List<String> answers;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Usuario usuario;
}