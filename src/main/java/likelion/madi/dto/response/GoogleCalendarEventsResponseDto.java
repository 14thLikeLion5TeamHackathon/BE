package likelion.madi.dto.response;

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

    private Long userId;
    private List<ScheduleItem> schedules;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        private Long scheduleId;
        private String title;
        private String eventDate; // YYYY-MM-DD
        private String eventTime; // HH:mm:ss
        private String location;
        private String source;    // "google"
        private String latitude;
        private String longitude;
    }
}