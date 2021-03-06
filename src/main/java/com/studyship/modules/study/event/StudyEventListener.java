package com.studyship.modules.study.event;

import com.studyship.infra.config.AppProperties;
import com.studyship.infra.mail.EmailMessage;
import com.studyship.infra.mail.EmailService;
import com.studyship.modules.account.Account;
import com.studyship.modules.account.AccountPredicates;
import com.studyship.modules.account.AccountRepository;
import com.studyship.modules.event.EventRepository;
import com.studyship.modules.notification.Notification;
import com.studyship.modules.notification.NotificationRepository;
import com.studyship.modules.notification.NotificationType;
import com.studyship.modules.study.Study;
import com.studyship.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendEmail(study, account, "새로운 스터디가 생겼습니다",
                        "스터디쉽, '" + study.getTitle() + "' 스터디가 생겼습니다.");
            }

            if (account.isStudyUpdatedByWeb()) {
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        HashSet<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendEmail(study, account, studyUpdateEvent.getMessage(),
                        "스터디쉽, '" + study.getTitle() + "' 스터디에 새소식이 있습니다.");
            }

            if (account.isStudyUpdatedByWeb()) {
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });
    }

    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}

