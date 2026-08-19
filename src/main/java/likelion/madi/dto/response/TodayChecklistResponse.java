package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayChecklistResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int completedCount;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalCount;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Builder
    public TodayChecklistResponse(int completedCount, int totalCount, List<Item> items) {
        this.completedCount = completedCount;
        this.totalCount = totalCount;
        this.items = items;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Item {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long checklistId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어카드가 없어 일정·날씨 기반으로 생성된 항목이면 null")
        private String sourceLabel;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private boolean completed;

        @Builder
        public Item(Long checklistId, String label, String sourceLabel, boolean completed) {
            this.checklistId = checklistId;
            this.label = label;
            this.sourceLabel = sourceLabel;
            this.completed = completed;
        }
    }
}
