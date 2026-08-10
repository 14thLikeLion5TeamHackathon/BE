package likelion.madi.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCardDetailResponse {
    private Long cardId;
    private String treatmentName;
    private LocalDate treatmentDate;
    private Integer dDay;
    private Integer recoveryTotalDays;
    private List<String> todayCare;
    private List<RecoveryGuideItem> recoveryGuide;
    private List<String> caution;
    private FeedbackQuota feedbackQuota;
    private VisitedStore visitedStore;

    @Builder
    public CareCardDetailResponse(Long cardId, String treatmentName, LocalDate treatmentDate, Integer dDay,
                                  Integer recoveryTotalDays, List<String> todayCare,
                                  List<RecoveryGuideItem> recoveryGuide, List<String> caution,
                                  FeedbackQuota feedbackQuota, VisitedStore visitedStore) {
        this.cardId = cardId;
        this.treatmentName = treatmentName;
        this.treatmentDate = treatmentDate;
        this.dDay = dDay;
        this.recoveryTotalDays = recoveryTotalDays;
        this.todayCare = todayCare;
        this.recoveryGuide = recoveryGuide;
        this.caution = caution;
        this.feedbackQuota = feedbackQuota;
        this.visitedStore = visitedStore;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecoveryGuideItem {
        private Integer dDayMin;
        private Integer dDayMax;
        private String label;
        private boolean isCurrent;

        @Builder
        public RecoveryGuideItem(Integer dDayMin, Integer dDayMax, String label, boolean isCurrent) {
            this.dDayMin = dDayMin;
            this.dDayMax = dDayMax;
            this.label = label;
            this.isCurrent = isCurrent;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FeedbackQuota {
        private long used;
        private int total;

        @Builder
        public FeedbackQuota(long used, int total) {
            this.used = used;
            this.total = total;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class VisitedStore {
        private Long storeId;
        private String name;
        private String address;
        private String url;
        private String latitude;
        private String longitude;

        @Builder
        public VisitedStore(Long storeId, String name, String address, String url,
                            String latitude, String longitude) {
            this.storeId = storeId;
            this.name = name;
            this.address = address;
            this.url = url;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}