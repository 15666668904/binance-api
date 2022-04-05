package com.szlong.balancemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author shanzhenlong
 */
@SpringBootApplication
public class BalanceMonitorApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(BalanceMonitorApplication.class, args);
        //保持程序运行
        Thread.currentThread().join();
    }

}
