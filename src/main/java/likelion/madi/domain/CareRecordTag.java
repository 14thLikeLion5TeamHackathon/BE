package likelion.madi.domain;

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

    public CareRecordTag(CareRecord careRecord, StatusTag statusTag) {
        this.careRecord = careRecord;
        this.statusTag = statusTag;
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
