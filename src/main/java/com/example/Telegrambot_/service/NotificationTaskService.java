package com.example.Telegrambot_.service;

import com.example.Telegrambot_.model.NotificationTask;
import org.springframework.stereotype.Service;
import com.example.Telegrambot_.repository.NotificationTaskRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public void addTask(LocalDateTime localDateTime,
                        String message,
                        Long userId) {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDataTime(localDateTime);
        notificationTask.setText(message);
        notificationTask.setUserId(userId);
        notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> findTasks() {
        return notificationTaskRepository.findNotificationTasksByDataTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    public void deleteTask(NotificationTask notificationTask) {
        notificationTaskRepository.delete(notificationTask);
    }
}
