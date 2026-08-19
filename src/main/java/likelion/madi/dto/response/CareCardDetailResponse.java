package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCardDetailResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String treatmentName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate treatmentDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dDay;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "매칭된 시술 정보가 없으면 null")
    private Integer recoveryTotalDays;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "매칭된 시술 정보가 없으면 null")
    private Integer recoveryTransitionDay;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> todayCare;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private FeedbackQuota feedbackQuota;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "방문 매장 정보가 없으면 null")
    private VisitedStore visitedStore;

    @Builder
    public CareCardDetailResponse(Long cardId, String treatmentName, LocalDate treatmentDate, Integer dDay,
                                  Integer recoveryTotalDays, Integer recoveryTransitionDay, List<String> todayCare,
                                  FeedbackQuota feedbackQuota, VisitedStore visitedStore) {
        this.cardId = cardId;
        this.treatmentName = treatmentName;
        this.treatmentDate = treatmentDate;
        this.dDay = dDay;
        this.recoveryTotalDays = recoveryTotalDays;
        this.recoveryTransitionDay = recoveryTransitionDay;
        this.todayCare = todayCare;
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
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private long used;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long storeId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String address;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String latitude;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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