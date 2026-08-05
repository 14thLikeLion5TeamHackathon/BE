package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import likelion.madi.enums.ScheduleSource;

import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "SCHEDULE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "location", length = 255)
    private String location;

    // 캘린더 연동으로 가져온 일정인지, 사용자가 직접 입력한 일정인지 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private ScheduleSource source;

    @Column(name = "latitude", length = 255)
    private String latitude;

    @Column(name = "longitude", length = 255)
    private String longitude;

    @Builder
    public Schedule(User user, String title, LocalDate eventDate, LocalTime eventTime,
                     String location, ScheduleSource source, String latitude, String longitude) {
        this.user = user;
        this.title = title;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.source = source;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 직접 입력한 일정만 수정 가능 (외부 캘린더 연동 일정은 읽기 전용)
    public void update(String title, LocalDate eventDate, LocalTime eventTime, String location) {
        this.title = title;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
    }
}
