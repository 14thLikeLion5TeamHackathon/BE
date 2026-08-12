package likelion.madi.dto.response;

import likelion.madi.domain.Schedule;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleResponse {
    private Long scheduleId;
    private String title;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String location;
    private String source;

    @Builder
    public ScheduleResponse(Long scheduleId, String title, LocalDate eventDate, LocalTime eventTime, String location, String source) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.source = source;
    }

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .title(schedule.getTitle())
                .eventDate(schedule.getEventDate())
                .eventTime(schedule.getEventTime())
                .location(schedule.getLocation())
                .source(schedule.getSource().name())
                .build();
    }
}
