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
@Table(name = "AAC_STORE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AacStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude", length = 255)
    private String latitude;

    @Column(name = "longitude", length = 255)
    private String longitude;

    @Builder
    public AacStore(String name, String address, String latitude, String longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
