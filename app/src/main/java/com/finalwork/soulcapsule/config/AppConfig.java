package com.finalwork.soulcapsule.config;

/**
 * 全局应用配置。部署时只需修改 BASE_URL 即可切换服务器环境。
 */
public final class AppConfig {

    /** 后端 API 基准地址（模拟器访问本机 localhost 使用 10.0.2.2） */
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    public static final long CONNECT_TIMEOUT_SECONDS = 15L;
    public static final long READ_TIMEOUT_SECONDS = 15L;
    public static final long WRITE_TIMEOUT_SECONDS = 15L;

    private AppConfig() {
    }
}
