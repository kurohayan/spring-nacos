package com.kuroha.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.kuroha.service.CloudService;
import com.kuroha.utility.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 服务调用 集成自实现路由表
 * @author kuroha
 * @date 2019-12-07 14:10:30
 */
@Slf4j
@Service
@EnableAsync
public class CloudServiceImpl implements CloudService {

    private final ConcurrentHashMap<String, List<String>> routingMap = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final ReentrantLock lock = new ReentrantLock();
    private final LoadingCache<String, AtomicInteger> cache;

    @NacosInjected
    private NamingService namingService;

    public CloudServiceImpl() {
        cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, AtomicInteger>() {
            @Override
            public AtomicInteger load(String key) throws Exception {
                return new AtomicInteger();
            }
        });
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(30000);
        restTemplate = new RestTemplate(factory);
    }

    /**
     * 服务调用get方法,默认返回String
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @return
     */
    @Override
    public String get(String serviceName, String apiName) {
        return this.get(serviceName,apiName,String.class);
    }
    /**
     * 服务调用post方法,默认返回String
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @param body 数据,HttpEntry或者String,Map都可
     * @return
     */
    @Override
    public String post(String serviceName, String apiName, Object body) {
        return this.post(serviceName,apiName,body,String.class);
    }
    /**
     * 服务调用get方法,返回传入的T类型
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @return
     */
    @Override
    public <T> T get(String serviceName, String apiName, Class<T> clazz) {
        String uri = this.getUri(serviceName);
        if (StringUtil.isBlank(uri)) {
            return null;
        }
        try {
            return restTemplate.getForObject(uri + apiName, clazz);
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            Set<String> uriSet = new HashSet<>();
            uriSet.add(uri);
            while (true) {
                try {
                    uri = checkServiceNameAndGetUri(serviceName,uri,uriSet);
                    if (StringUtil.isBlank(uri)){
                        return null;
                    }
                    return restTemplate.getForObject(uri + apiName, clazz);
                }catch (Exception ex) {
                    uriSet.add(uri);
                    log.error(ex.getMessage(),ex);
                }
            }
        }
    }
    /**
     * 服务调用post方法,返回传入的T类型
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @param body 数据,HttpEntry或者String,Map都可
     * @return
     */
    @Override
    public <T> T post(String serviceName, String apiName, Object body, Class<T> clazz) {
        String uri = this.getUri(serviceName);
        if (StringUtil.isBlank(uri)) {
            return null;
        }
        try {
            return restTemplate.postForObject(uri + apiName,body,clazz);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            Set<String> uriSet = new HashSet<>();
            uriSet.add(uri);
            while (true) {
                try {
                    uri = checkServiceNameAndGetUri(serviceName,uri,uriSet);
                    if (StringUtil.isBlank(uri)){
                        return null;
                    }
                    return restTemplate.postForObject(uri + apiName,body, clazz);
                }catch (Exception ex) {
                    uriSet.add(uri);
                    log.error(ex.getMessage(),ex);
                }
            }
        }
    }
    /**
     * 服务调用put方法,直接返回调用是否成功,不返回具体的值
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @return
     */
    @Override
    public boolean put(String serviceName, String apiName, Object body) {
        String uri = this.getUri(serviceName);
        if (StringUtil.isBlank(uri)) {
            return false;
        }
        try {
            restTemplate.put(uri + apiName,body);
            return true;
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            Set<String> uriSet = new HashSet<>();
            uriSet.add(uri);
            while (true) {
                try {
                    uri = checkServiceNameAndGetUri(serviceName,uri,uriSet);
                    if (StringUtil.isBlank(uri)){
                        return false;
                    }
                    restTemplate.put(uri + apiName,body);
                    return true;
                }catch (Exception ex) {
                    uriSet.add(uri);
                    log.error(ex.getMessage(),ex);
                }
            }
        }
    }
    /**
     * 服务调用delete方法,直接返回调用是否成功,不返回具体的值
     * @param serviceName 服务名
     * @param apiName 服务接口名
     * @return
     */
    @Override
    public boolean delete(String serviceName, String apiName) {
        String uri = this.getUri(serviceName);
        if (StringUtil.isBlank(uri)) {
            return false;
        }
        try {
            restTemplate.delete(uri + apiName);
            return true;
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            Set<String> uriSet = new HashSet<>();
            uriSet.add(uri);
            while (true) {
                try {
                    uri = checkServiceNameAndGetUri(serviceName,uri,uriSet);
                    if (StringUtil.isBlank(uri)){
                        return false;
                    }
                    restTemplate.delete(uri + apiName);
                    return true;
                }catch (Exception ex) {
                    uriSet.add(uri);
                    log.error(ex.getMessage(),ex);
                }
            }
        }
    }

    /**
     * 获取实际访问的uri
     * @param serviceName 服务名
     * @return uri
     */
    private String getUri(String serviceName) {
        List<String> list = routingMap.get(serviceName);
        // 判空
        if (list == null || list.size()==0) {
            // 加锁
            lock.lock();
            try {
                list = routingMap.get(serviceName);
                // 再次判空
                if (list == null || list.size()==0) {
                    // 根据服务名从nacos获取uri列表
                    list = this.getServiceList(serviceName);
                    routingMap.put(serviceName, list);
                }
            }finally {
                // 解锁
                lock.unlock();
            }
        } else {
            // 非空直接返回
            return list.get(random.nextInt(list.size()));
        }
        // 再次结束后判空名
        if (list.size()==0) {
            return null;
        }
        String uri = list.get(random.nextInt(list.size()));
        log.debug("成功寻找倒uri:" + uri);
        // 非空返回
        return uri;
    }
    /**
     * 获取除去uri的实际访问的uri
     * @param serviceName 服务名
     * @param uriSet 本次调用失败的ip地址
     * @return uri
     */
    private String getUriThrow(String serviceName, Set<String> uriSet) {
        List<String> list = routingMap.get(serviceName);
        list = list.stream().filter(s -> !uriSet.contains(s)).collect(Collectors.toList());
        if (list.size() == 0) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * 定时初始化路由表,用于将不健康的实例下线,或者将健康的实例再次上线
     * 初始化路由表
     */
    @Async
    @Scheduled(fixedDelay = 60000)
    @Override
    public void initRoutingMap() {
        log.debug("初始化路由表");
        routingMap.forEach((key,value)->{
            List<String> uriList = this.getServiceList(key);
            if (uriList.size() == 0) {
                routingMap.remove(key);
            } else {
                routingMap.put(key, uriList);
            }
        });
    }

    /**
     * 获取全部路由表
     * @return
     */
    @Override
    public String getAllUri() {
        return JSON.toJSONString(routingMap);
    }

    /**
     * 检测服务不生效次数
     * @param serviceName
     * @param uri
     * @param uriSet
     * @return
     * @throws ExecutionException
     */
    private String checkServiceNameAndGetUri(String serviceName, String uri, Set<String> uriSet) throws ExecutionException {
        AtomicInteger num = cache.get(serviceName);
        if (num == null) {
            cache.put(serviceName, new AtomicInteger(1));
        } else if(num.get() < 5) {
            num.incrementAndGet();
        } else {
            cache.put(serviceName, new AtomicInteger(0));
            List<String> collect = routingMap.get(serviceName).stream().parallel().filter(s -> !s.equals(uri)).collect(Collectors.toList());
            routingMap.put(serviceName,collect);
        }
        return getUriThrow(serviceName, uriSet);
    }

    /**
     * 获取服务列表
     * @param serviceName
     * @return
     */
    private List<String> getServiceList(String serviceName) {
        int randNum = 100;
        List<String> list = new ArrayList<>(0);
        try {
            // 获取指定serviceName的路由信息
            List<Instance> instanceList = namingService.getAllInstances(serviceName);
            double sum = 0;
            // 对可能的情况进行处理
            if (instanceList.size() == 0) {
                return list;
            } else if (instanceList.size() == 1) {
                list = new ArrayList<>(1);
                Instance instance = instanceList.get(0);
                // 非启用或者非健康的跳过
                if (!(instance.isHealthy() && instance.isEnabled())) {
                    return list;
                }
                list.add(StringUtil.splicingString("http://",instance.getIp(),":",instance.getPort(),"/"));
            } else {
                list = new ArrayList<>((int)(randNum * 1.1));
                for (Instance instance : instanceList) {
                    // 非启用或者非健康的跳过
                    if (!(instance.isHealthy() && instance.isEnabled())) {
                        continue;
                    }
                    double weight = instance.getWeight();
                    sum += weight;
                }
                for (Instance instance : instanceList) {
                    // 非启用或者非健康的跳过
                    if (!(instance.isHealthy() && instance.isEnabled())) {
                        continue;
                    }
                    String uri = StringUtil.splicingString("http://",instance.getIp(),":",instance.getPort(),"/");
                    double weight = instance.getWeight();
                    int num = (int)Math.round(weight* randNum /sum);
                    for (int i = 0; i < num; i++) {
                        list.add(uri);
                    }
                }
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return list;
    }
}
