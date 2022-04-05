package com.szlong.balancemonitor.client;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author shanzhenlong
 */
@ConfigurationProperties(prefix = "telegram")
@ConditionalOnProperty(prefix = "telegram", value = "enable", havingValue = "true")
@Configuration
public class TelegramClient {

    private String botToken;
    private String apiUrl;
    private String chatId;

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * send message url
     */
    private static String sendMessageUrl;

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @PostConstruct
    public void init() {
        sendMessageUrl = StrUtil.concat(false, apiUrl, "/bot", botToken, "/sendMessage");
    }

    /**
     * send telegram message
     *
     * @param message message
     */
    public void sendMessage(String message) {
        HttpResponse response = HttpUtil.createPost(sendMessageUrl)
                .body(
                        new JSONObject() {{
                            put("chat_id", chatId);
                            put("text", message);
                        }}
                                .toString())
                .execute();
        Log.get().info("send request [{}] response [{}]", sendMessageUrl, response);
    }
}
