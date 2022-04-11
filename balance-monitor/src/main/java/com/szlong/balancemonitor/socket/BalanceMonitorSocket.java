package com.szlong.balancemonitor.socket;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.binance.connector.client.WebsocketClient;
import com.binance.connector.client.impl.spot.Market;
import com.binance.connector.client.impl.spot.UserData;
import com.binance.connector.client.utils.WebSocketCallback;
import com.szlong.balancemonitor.service.INotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author shanzhenlong
 */
@Component
@ConditionalOnBean(WebsocketClient.class)
public record BalanceMonitorSocket(WebsocketClient websocketClient,
                                   UserData userData,
                                   List<INotificationService> notificationServices,
                                   Market market) {
    /**
     * USDT
     */
    private static final String USDT = "USDT";
    /**
     * info socket handler
     */
    private static final WebSocketCallback INFO_WEB_SOCKET_CALLBACK = data -> Log.get().info("info webSocket callback [{}]", data);
    /**
     * event
     */
    private static final String EVENT_NAME = "balanceUpdate";
    /**
     * message
     */
    private static final String MESSAGE_TEMPLATE = """
            =====================
            balance changed!!!

            coin: [{}]
            delta:[{}]
            delta(USDT): [{}]
            =====================
            """;
    /**
     * format
     */
    private static final String FORMAT = ",##0.00";
    /**
     * listen key
     */
    private static String listenKey = StrUtil.EMPTY;
    /**
     * timer
     */
    private static final Timer TIMER = new Timer("binance-listen-key-refresher", true);

    private void createRefreshListenKeyJob() {
        TIMER.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Log.get()
                                    .info(
                                            "extendListenKey [{}] [{}]",
                                            listenKey,
                                            userData.extendListenKey(new LinkedHashMap<>() {{
                                                put("listenKey", listenKey);
                                            }})
                                    );
                        } catch (Exception e) {
                            Log.get().error("refresh listenKey [{}] err", listenKey, e);
                        }
                    }
                },
                0L,
                20L * 1000L * 60L
        );
    }

    @PostConstruct
    public void init() {
        //服务启动 首次运行时 创建一个job刷新listenKey
        boolean needSchedule = listenKey.equals(StrUtil.EMPTY);
        listenKey = JSONUtil.parseObj(userData.createListenKey()).getStr("listenKey");
        if (needSchedule) {
            createRefreshListenKeyJob();
        }
        //2022-04-04 18:20:09.194[1649067609194] | INFO  | OkHttp https://stream.binance.com:9443/... | c.s.b.socket.BalanceMonitorSocket    - onReceive [{"e":"balanceUpdate","E":1649067609172,"a":"BNB","d":"-1.00000000","T":1649067609171}]
        //2022-04-04 18:20:09.195[1649067609195] | INFO  | OkHttp https://stream.binance.com:9443/... | c.s.b.socket.BalanceMonitorSocket    - onReceive [{"e":"outboundAccountPosition","E":1649067609172,"u":1649067609171,"B":[{"a":"BNB","f":"12.02098209","l":"0.00000000"}]}]
        websocketClient.listenUserStream(
                listenKey,
                INFO_WEB_SOCKET_CALLBACK,
                data -> {
                    try {
                        Log.get().info("received message [{}]", data);
                        JSONObject message = JSONUtil.parseObj(data);
                        if (EVENT_NAME.equals(message.getStr("e"))) {
                            String asset = message.getStr("a");
                            BigDecimal delta = message.getBigDecimal("d");
                            notificationServices.forEach(
                                    n -> n.sendNotification(
                                            StrUtil.format(
                                                    MESSAGE_TEMPLATE,
                                                    asset,
                                                    delta.stripTrailingZeros().toPlainString(),
                                                    NumberUtil.decimalFormat(FORMAT, delta.multiply(getUsdtPrice(asset)))
                                            )
                                    )
                            );
                        }
                    } catch (Exception e) {
                        Log.get().error("on message err [{}]", data, e);
                    }
                }
                ,
                data -> {
                    Log.get().info("onClosingCallback [{}]", data);
                    // reinit
                    init();
                },
                INFO_WEB_SOCKET_CALLBACK
        );
    }

    /**
     * get usdt price
     */
    private BigDecimal getUsdtPrice(String asset) {
        if (USDT.equals(asset)) {
            return BigDecimal.ONE;
        }
        String symbol = StrUtil.concat(false, asset, USDT);
        try {
            return JSONUtil.parseObj(
                            market.tickerSymbol(
                                    new LinkedHashMap<>() {{
                                        put("symbol", symbol);
                                    }}
                            )
                    )
                    .getBigDecimal("price");
        } catch (Exception e) {
            Log.get().error("get usdt price err [{}]", asset, e);
        }
        return BigDecimal.ZERO;
    }
}
