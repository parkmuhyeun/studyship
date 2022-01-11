package com.studyship.modules.event.event;

import com.studyship.modules.account.Account;
import com.studyship.modules.event.Enrollment;
import com.studyship.modules.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EnrollmentEvent {

    private final Enrollment enrollment;

    private final String message;
}
