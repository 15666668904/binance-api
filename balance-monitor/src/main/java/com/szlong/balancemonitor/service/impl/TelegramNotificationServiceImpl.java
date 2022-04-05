package com.szlong.balancemonitor.service.impl;

import com.szlong.balancemonitor.client.TelegramClient;
import com.szlong.balancemonitor.service.INotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * @author shanzhenlong
 */
@Service
@ConditionalOnBean(TelegramClient.class)
public class TelegramNotificationServiceImpl implements INotificationService {

    private final TelegramClient telegramClient;

    public TelegramNotificationServiceImpl(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void sendNotification(String text) {
        telegramClient.sendMessage(text);
    }
}
