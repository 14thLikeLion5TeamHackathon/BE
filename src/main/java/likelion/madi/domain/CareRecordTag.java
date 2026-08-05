package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "CARE_RECORD_TAG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordTag {

    @EmbeddedId
    private CareRecordTagId id;

    @MapsId("recordId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private StatusTag statusTag;

    // 태그별 증상 강도: 0(없음)~3(심함). 회복 탭 증상 곡선과 상담 권고 규칙의 판단 근거로 사용
    @Column(name = "intensity")
    private Integer intensity;

    public CareRecordTag(CareRecord careRecord, StatusTag statusTag, Integer intensity) {
        this.careRecord = careRecord;
        this.statusTag = statusTag;
        this.intensity = intensity;
        this.id = new CareRecordTagId(careRecord.getRecordId(), statusTag.getTagId());
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    public static class CareRecordTagId implements Serializable {
        private Long recordId;
        private Long tagId;

        public CareRecordTagId(Long recordId, Long tagId) {
            this.recordId = recordId;
            this.tagId = tagId;
        }
    }
}
