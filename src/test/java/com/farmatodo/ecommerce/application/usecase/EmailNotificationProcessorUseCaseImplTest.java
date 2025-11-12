package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.domain.model.EmailNotification;
import com.farmatodo.ecommerce.domain.port.out.EmailNotificationRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.EmailSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationProcessorUseCaseImplTest {

    @Mock
    private EmailNotificationRepositoryPort notificationRepository;
    @Mock
    private EmailSenderPort emailSender;

    @InjectMocks
    private EmailNotificationProcessorUseCaseImpl emailProcessorUseCase;

    @Test
    void whenProcessPendingEmails_withOnePendingEmail_shouldSendAndMarkAsSent() throws Exception {
        EmailNotification pendingEmail = EmailNotification.builder()
                .id(1L)
                .emailTo("test@example.com")
                .emailSubject("Test")
                .emailBody("Body")
                .status(EmailNotification.EmailStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
        List<EmailNotification> pendingList = Collections.singletonList(pendingEmail);

        when(notificationRepository.findPendingEmailsToRetry()).thenReturn(pendingList);
        doNothing().when(emailSender).sendEmail(anyString(), anyString(), anyString());
        emailProcessorUseCase.processPendingEmails();
        verify(emailSender, times(1)).sendEmail("test@example.com", "Test", "Body");
        verify(notificationRepository, times(1)).save(argThat(
                notification -> notification.getStatus() == EmailNotification.EmailStatus.SENT &&
                        notification.getSentAt() != null
        ));
    }

    @Test
    void whenProcessPendingEmails_andSenderFails_shouldIncrementRetryAndMarkAsPending() throws Exception {
        EmailNotification pendingEmail = EmailNotification.builder()
                .id(1L)
                .emailTo("test@example.com")
                .status(EmailNotification.EmailStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();

        when(notificationRepository.findPendingEmailsToRetry()).thenReturn(Collections.singletonList(pendingEmail));
        doThrow(new RuntimeException("SMTP Server down"))
                .when(emailSender).sendEmail(
                        eq("test@example.com"),
                        nullable(String.class),
                        nullable(String.class)
                );
        emailProcessorUseCase.processPendingEmails();
        verify(emailSender, times(1))
                .sendEmail(eq("test@example.com"), nullable(String.class), nullable(String.class));
        verify(notificationRepository, times(1)).save(argThat(
                notification -> notification.getStatus() == EmailNotification.EmailStatus.PENDING &&
                        notification.getRetryCount() == 1 &&
                        notification.getErrorMessage().equals("SMTP Server down")
        ));
    }

    @Test
    void whenProcessPendingEmails_andSenderFails_andMaxRetriesReached_shouldMarkAsFailed() throws Exception {
        EmailNotification pendingEmail = EmailNotification.builder()
                .id(1L)
                .emailTo("test@example.com")
                .status(EmailNotification.EmailStatus.PENDING)
                .retryCount(2)
                .maxRetries(3)
                .build();

        when(notificationRepository.findPendingEmailsToRetry()).thenReturn(Collections.singletonList(pendingEmail));
        doThrow(new RuntimeException("SMTP Server down"))
                .when(emailSender).sendEmail(
                        eq("test@example.com"),
                        nullable(String.class),
                        nullable(String.class)
                );

        emailProcessorUseCase.processPendingEmails();
        verify(emailSender, times(1)).sendEmail(eq("test@example.com"), nullable(String.class), nullable(String.class));
        verify(notificationRepository, times(1)).save(argThat(
                notification -> notification.getStatus() == EmailNotification.EmailStatus.FAILED &&
                        notification.getRetryCount() == 3
        ));
    }

    @Test
    void whenProcessPendingEmails_withNoPendingEmails_shouldDoNothing() throws Exception {
        when(notificationRepository.findPendingEmailsToRetry()).thenReturn(Collections.emptyList());
        emailProcessorUseCase.processPendingEmails();
        verify(emailSender, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationRepository, never()).save(any(EmailNotification.class));
    }
}