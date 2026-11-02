package nextvest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "legal_representative")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalRepresentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(nullable = false, length = 45)
    private String lastName;

    @Column(nullable = false)
    private Integer dni;

    @Column(nullable = false, length = 45)
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}