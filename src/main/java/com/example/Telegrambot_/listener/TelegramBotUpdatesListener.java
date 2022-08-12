package com.example.Telegrambot_.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.Telegrambot_.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private static final Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long user = update.message().chat().id();
            String message = update.message().text();
            if (message.equals("/start")) {
                SendMessage sendMessage = new SendMessage(user, "Отправьте в формате \n*01.01.2022 20:00 Сделать домашнюю работу*");
                sendMessage.parseMode(ParseMode.Markdown);
                telegramBot.execute(sendMessage);
            } else {
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    LocalDateTime localDateTime = parselocalDateTime(matcher.group(1));
                    if (localDateTime != null) {
                        String message1 = matcher.group(3);
                        notificationTaskService.addTask(localDateTime, message1, user);
                        telegramBot.execute(new SendMessage(user, "Задача запланирована"));
                    } else {
                        SendMessage sendMessage = new SendMessage(user, "Ошибка ввода");
                        sendMessage.parseMode(ParseMode.Markdown);
                        telegramBot.execute(sendMessage);
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        notificationTaskService.findTasks().
                forEach(notificationTask -> {
                    telegramBot.execute(
                            new SendMessage(notificationTask.getUserId(), "напоминание " + notificationTask.getText())
                    ); notificationTaskService.deleteTask(notificationTask);
                });
    }

    @Nullable
    private LocalDateTime parselocalDateTime(String localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        try {
            return LocalDateTime.parse(localDateTime, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
