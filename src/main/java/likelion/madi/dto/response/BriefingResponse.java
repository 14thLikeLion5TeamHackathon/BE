package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String date;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ScheduleItem> schedules;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어카드가 없으면 null")
    private CardJudgement cardJudgement;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어카드가 없으면 null")
    private String overallCautionLevel;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean calendarConnected;

    @Builder
    public BriefingResponse(String date,
                             List<ScheduleItem> schedules, CardJudgement cardJudgement,
                             String overallCautionLevel, boolean calendarConnected) {
        this.date = date;
        this.schedules = schedules;
        this.cardJudgement = cardJudgement;
        this.overallCautionLevel = overallCautionLevel;
        this.calendarConnected = calendarConnected;
    }

    @Getter @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class ScheduleItem {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long scheduleId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "일정에 시간 정보가 없으면 null")
        private String time;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String location;
    }

    // 카드가 여러 장이어도 전부 반영해서 문장 하나로 합쳐서 나옴 (카드별로 따로 안 나옴)
    @Getter @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class CardJudgement {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> cardIds;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String actionSentence;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String cautionLevel;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<String> reasons;
    }
}
