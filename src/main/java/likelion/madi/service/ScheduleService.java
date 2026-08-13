package likelion.madi.service;

import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.Schedule;
import likelion.madi.domain.User;
import likelion.madi.dto.request.ScheduleCreateRequest;
import likelion.madi.dto.request.ScheduleUpdateRequest;
import likelion.madi.dto.response.ScheduleResponse;
import likelion.madi.enums.ScheduleSource;
import likelion.madi.repository.ScheduleRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional
    public ScheduleResponse create(Long userId, ScheduleCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(request.getTitle())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .location(request.getLocation())
                .source(ScheduleSource.MANUAL)
                .build();

        scheduleRepository.save(schedule);

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse update(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_SCHEDULE));

        if (!schedule.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        if (schedule.getSource() != ScheduleSource.MANUAL) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_SCHEDULE_NOT_EDITABLE);
        }

        schedule.update(request.getTitle(), request.getEventDate(), request.getEventTime(), request.getLocation());

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_SCHEDULE));

        if (!schedule.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        if (schedule.getSource() != ScheduleSource.MANUAL) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_SCHEDULE_NOT_EDITABLE);
        }

        scheduleRepository.delete(schedule);
    }
}