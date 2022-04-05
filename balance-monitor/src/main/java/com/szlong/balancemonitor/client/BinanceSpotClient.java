package com.szlong.balancemonitor.client;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebsocketClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.binance.connector.client.impl.spot.Market;
import com.binance.connector.client.impl.spot.UserData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shanzhenlong
 */
@ConfigurationProperties(prefix = "binance")
@Configuration
@ConditionalOnProperty(prefix = "binance", value = {"api-key", "secret-key"})
public class BinanceSpotClient {
    private String apiKey;
    private String secretKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Bean
    public SpotClient spotClient() {
        return new SpotClientImpl(apiKey, secretKey);
    }

    @Bean
    public UserData userData(SpotClient spotClient) {
        return spotClient.createUserData();
    }

    @Bean
    public WebsocketClient websocketClient(UserData userData) {
        return new WebsocketClientImpl();
    }

    @Bean
    public Market market(SpotClient spotClient) {
        return spotClient.createMarket();
    }
}
