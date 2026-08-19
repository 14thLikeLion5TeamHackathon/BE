package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scheduleId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate eventDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime eventTime;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
