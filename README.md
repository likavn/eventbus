## 什么是 notify-spring-boot-starter？

notify-spring-boot-starter消息组件，支持分布式业务消息总线、延时消息等，屏蔽底层不同种类的消息引擎，提供统一调用接口，可发送异步消息及延时消息，同时可订阅异步消息或延时消息，降低系统耦合度。目前可选择基于redis、rabbitmq等任一一种做消息引擎，其他消息中间件将陆续支持。



### 支持的消息引擎中间件

- redis
- rabbitmq



## 有哪些特点？

我们不是另外开发一个Mq，而是屏蔽底层不同类型的中间件，统一接口调用。

## 有哪些功能？

异步业务消息订阅、广播及延时消息订阅，支持消息投递失败重试等。

## 有哪些场景可以使用？

单一业务分发消息进行异步处理时，比如业务完成推送业务数据给第三方；

支付时，后端服务需要定时轮训支付接口查询是否支付成功；

系统业务消息传播解耦；

## 版本要求

1.springBoot 2.3.0.RELEASE+

2.redis 5.0+

3.rabbitMq 3.8.3+

## 快速开始

### 引入依赖

```xml

<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>notify-spring-boot-starter</artifactId>
    <version>1.6.1</version>
</dependency>
```

### 设置消息引擎类别

在application.yml文件中配置消息引擎类别，如下：

```yaml
notify:
  type: redis  #redis或者rabbitmq
```

#### redis

使用Redis5.0 新功能Stream，Redis Stream 提供了消息的持久化和主备复制功能，可以让任何客户端访问任何时刻的数据，并且能记住每一个客户端的访问位置，还能保证消息不丢失。

需要在pom.xml单独引入，如下：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### rabbitmq

rabbitmq，需要在pom.xml单独引入，如下：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```



### 发送与订阅异步消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
// 第一个参数是业务消息code
// 第二个参数是业务消息Object实体对象数据
msgSender.send("testMsgSubscribe", "charging");
```



订阅异步业务消息监听器实现类SubscribeMsgDemoListener.java

```java
/**
 * 订阅异步消息
 * 继承超类【SubscribeMsgListener】并设置监听的消息实体对象
 */
@Slf4j
@Component
public class SubscribeMsgDemoListener extends SubscribeMsgListener<String> {

    /**
     * 必须有一个构造函数订阅业务消息类型
     */
    public SubscribeMsgDemoListener() {
        // 设置订阅的业务消息类型，其他类型的服务的消息类型，可设置对应服务id+业务消息类型code
        super(Collections.singletonList("testMsgSubscribe"));
    }

    /**
     * 接收业务消息体对象数据
     */
    @Override
    public void accept(Message<String> message) {
        log.info("消息监听,body:{}", message.getBody());
    }
}
```

### 发送与订阅延时消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
// 第一个参数 【DelayMsgDemoListener.class】为当前延时消息的处理实现类
// 第二个参数为延时消息体
// 第三个参数为延时时间，单位：秒
msgSender.sendDelayMessage(DelayMsgDemoListener.class, "922321333", 5);
```

延时消息监听器实现类DelayMsgDemoListener.java

```java
/**
 * 订阅延时消息
 * 实现接口【DelayMsgListener】并设置回调消息body实体
 */
@Slf4j
@Component
public class DelayMsgDemoListener implements DelayMsgListener<String> {
    @Override
    public void onMessage(Message<String> message) {
        log.info("接收延时消息回调body:{}", message.getBody());
    }
}
```



### 异常捕获

当订阅消息或延时消息投递失败时，可以自定义消息重复投递次数和下次消息投递时间间隔（系统默认重复投递3次，每次间隔3秒），即便这样，消息还是有可能会存在投递不成功的问题，当消息进行最后一次投递还是失败时，可以使用注解`@FailCallback`
标识在订阅或延时消息处理类的异常处理方法上，到达最大重复投递次数且还是投递失败时调用此方法，即可捕获投递错误异常及数据。如下：

```java

@Slf4j
@Component
public class SubscribeMsgDemoListener extends SubscribeMsgListener<String> {

    /**
     * 必须有一个构造函数订阅业务消息类型
     */
    public SubscribeMsgDemoListener() {
        // 设置订阅的业务消息类型，其他类型的服务的消息类型，可设置对应服务id+业务消息类型code
        super(Collections.singletonList("testMsgSubscribe"), 5, 10L);
    }

    /**
     * 接收业务消息体对象数据
     */
    @Override
    public void accept(Message<String> message) {
        log.info("消息监听,body:{}", message.getBody());
        // throw new RuntimeException("接收失败测试...");
    }

    /**
     * 到达最大重复投递次数且还是投递失败时调用此方法，参数列表不分顺序
     */
    @FailCallback
    public void error(Message<String> message, Exception exception) {
        log.info("失败回调");
    }
}
```

## 注意事项

**订阅、广播消息在消息引擎中是以订阅器实现类全类名进行分组（在rabbitMq中的存在是队列），当我们不在需要某个订阅器时请及时在消息引擎中删除此分组或队列，避免不必要的存储空间浪费。**

更多信息请查阅相关接口类...

项目地址：[https://github.com/likavn/notify-spring-boot-starter](https://github.com/likavn/notify-spring-boot-starter)

