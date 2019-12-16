package com.kuroha.service;

/**
 * @author kuroha
 */
public interface CloudService {

    String get(String serviceName, String apiName);

    String post(String serviceName, String apiName, Object body);

    <T> T get(String serviceName, String apiName, Class<T> clazz);

    <T> T post(String serviceName, String apiName, Object body, Class<T> clazz);

    boolean put(String serviceName, String apiName, Object body);

    boolean delete(String serviceName, String apiName);

    void initRoutingMap();

    String getAllUri();

}
