package com.example.Telegrambot_.repository;

import com.example.Telegrambot_.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findNotificationTasksByDataTime(LocalDateTime localDateTime);
}
