package com.szlong.balancemonitor.service;

/**
 * @author shanzhenlong
 */
public interface INotificationService {
    /**
     * send notification
     *
     * @param text text
     */
    void sendNotification(String text);
}
