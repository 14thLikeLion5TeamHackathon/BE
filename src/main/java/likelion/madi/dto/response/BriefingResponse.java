package likelion.madi.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingResponse {
    private String date;
    private List<ScheduleItem> schedules;
    private CardJudgement cardJudgement;
    private String overallCautionLevel;
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
        private Long scheduleId;
        private String title;
        private String time;
        private String location;
    }

    // 카드가 여러 장이어도 전부 반영해서 문장 하나로 합쳐서 나옴 (카드별로 따로 안 나옴)
    @Getter @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class CardJudgement {
        private List<Long> cardIds;
        private String actionSentence;
        private String cautionLevel;
        private List<String> reasons;
    }
}
