package com.shang.distributelock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PartitionLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartitionLockApplication.class, args);
    }

}
