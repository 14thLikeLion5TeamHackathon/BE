package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CARE_RECORD_PHOTO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public CareRecordPhoto(CareRecord careRecord, String photoUrl, Integer sortOrder) {
        this.careRecord = careRecord;
        this.photoUrl = photoUrl;
        this.sortOrder = sortOrder;
    }
}
