package com.khoavdse170395.assignmentservice.config;

import org.springframework.stereotype.Component;

@Component
public class DemoFlags {
    private volatile boolean failReserve;

    public boolean isFailReserve() {
        return failReserve;
    }

    public void setFailReserve(boolean failReserve) {
        this.failReserve = failReserve;
    }
}





