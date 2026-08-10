package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TREATMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    private Long treatmentId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private AacStore store;


    @Builder
    public Treatment(String name, String category, String description, AacStore store) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.store = store;
    }

}
