package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarEventsResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long scheduleId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String eventDate; // YYYY-MM-DD
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String eventTime; // HH:mm:ss
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String location;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) // "google"
        private String source;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String latitude;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String longitude;
    }
}