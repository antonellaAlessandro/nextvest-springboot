package nextvest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "total_value")
    private Double totalValue;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Usuario usuario;
}