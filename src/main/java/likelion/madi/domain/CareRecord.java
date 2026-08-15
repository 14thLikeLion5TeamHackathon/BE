package likelion.madi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "CARE_RECORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CareCard careCard;

    // 더 이상 새 기록에는 쓰이지 않음. 여러 장 지원 이전에 저장된 기존 기록의 사진 마이그레이션용으로만 남겨둠 -> CareRecordPhotoMigrationLoader 참고
    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @OneToMany(mappedBy = "careRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareRecordPhoto> photos = new ArrayList<>();

    @Lob
    @Column(name = "status_description", columnDefinition = "TEXT")
    private String statusDescription;

    @Column(name = "d_day")
    private Integer dDay;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @OneToMany(mappedBy = "careRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareRecordTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "careRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiFeedback> aiFeedbacks = new ArrayList<>();

    @Builder
    public CareRecord(CareCard careCard, String photoUrl, String statusDescription, Integer dDay) {
        this.careCard = careCard;
        this.photoUrl = photoUrl;
        this.statusDescription = statusDescription;
        this.dDay = dDay;
        this.recordedAt = LocalDateTime.now();
    }

    public void addTag(CareRecordTag tag) {
        this.tags.add(tag);
    }

    public void addPhoto(CareRecordPhoto photo) {
        this.photos.add(photo);
    }

    public List<String> getPhotoUrls() {
        if (!photos.isEmpty()) {
            return photos.stream()
                    .sorted(Comparator.comparing(CareRecordPhoto::getSortOrder))
                    .map(CareRecordPhoto::getPhotoUrl)
                    .toList();
        }
        return photoUrl != null ? List.of(photoUrl) : List.of();
    }
}
