package com.studyship.event;

import com.studyship.domain.Account;
import com.studyship.domain.Event;
import com.studyship.domain.Study;
import com.studyship.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        //TODO 모집 인원을 늘린 선착순 모임의 경우에 자등으로 추가 확정
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }
}
