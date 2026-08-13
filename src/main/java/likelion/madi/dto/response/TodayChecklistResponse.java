package likelion.madi.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayChecklistResponse {
    private int completedCount;
    private int totalCount;
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
        private Long checklistId;
        private String label;
        private String sourceLabel;
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
