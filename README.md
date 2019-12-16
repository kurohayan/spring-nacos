##spring项目使用nacos服务发现及服务调用的功能
````
主要方法为com.kuroha.service.impl.CloudServiceImpl
实现的功能
1.从nacos中获取指定服务.并加载到routingMap中.
2.实现服务下线功能,暂时的实现方式为定时任务.每分钟一次更新服务,后期会更改为监听nacos服务变化.
3.每分钟服务调用失败5次后.会自动下线指定ip,等待下次服务更新
4.调用方法是否成功对用户无感知,直到服务调用成功为止,或者所以服务全部不可用为止
5.几个可更改的数字getServiceList 中

````