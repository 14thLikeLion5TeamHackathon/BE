package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "store_location", length = 255)
    private String storeLocation;

    @Builder
    public Treatment(String name, String category, String description, String storeName, String storeLocation) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
    }

}
