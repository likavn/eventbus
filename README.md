## eventbus是什么

eventbus是分布式业务消息分发总线组件，支持广播及时消息、延时消息等。通过屏蔽底层不同种类的消息引擎，提供统一的调用接口，可发送广播消息及延时消息，同时可订阅异步消息或延时消息，有利于降低系统耦合度。目前可选择基于Redis、RabbitMQ等任一
一种做消息引擎，其他消息中间件将被陆续支持。

注意：它不属于`消息中间件`，他是通过和消息中间件整合，来完成服务之间消息通讯，类似于消息代理。

### 支持的消息引擎中间件

- Redis
- RabbitMQ

计划支持RocketMQ、Pulsar

## 有哪些特点

我们不是另外开发一个MQ，而是屏蔽底层不同种类的消息中间件，统一整合接口调用。

## 有哪些功能

- 消息：支持广播消息、延时消息的投递和接收，可通过接口或注解方式去订阅接收消息；

- 重试：支持消息投递失败时投递重试，重试投递次数及下次投递时间可自定义；

- 拦截器：支持全局拦截器，可自主实现拦截逻辑，支持发送前拦截（`SendBeforeInterceptor `）、发送后拦截（`SendAfterInterceptor `
  ）、投递成功后拦截（`DeliverSuccessInterceptor `）、投递失败时拦截（`DeliverThrowableInterceptor `）；

## 有哪些场景可以使用

单一业务分发消息进行异步处理时，比如业务完成推送业务数据给第三方；

支付时，后端服务需要定时轮训支付接口查询是否支付成功；

系统业务消息传播解耦，降低消息投递和接收的复杂度；

## 版本要求

1.SpringBoot 2.5.0.RELEASE+

2.Redis 5.0+

3.RabbitMQ 3.8.3+

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>eventbus-spring-boot-starter</artifactId>
    <version>2.0</version>
</dependency>
```

### 设置消息引擎类别

在application.yml文件中配置消息引擎种类，如下：

```yaml
eventbus:
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
<dependency>
 	<groupId>org.apache.commons</groupId>
	<artifactId>commons-pool2</artifactId>
</dependency>
```

#### RabbitMQ

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

订阅异步业务消息监听器实现类DemoMsgSubscribeListener.java

```java
/**
 * 订阅异步消息
 * 继承超类【MsgSubscribeListener】并设置监听的消息实体对象
 */
@Slf4j
@Component
public class DemoMsgSubscribeListener extends MsgSubscribeListener<String> {
    protected DemoMsgSubscribeListener() {
        // 订阅消息code
        super("testMsgSubscribe");
    }

    // 接收业务消息体对象数据
    @Override
    public void onMessage(Message<String> message) {
        String body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        // throw new RuntimeException("DemoMsgSubscribeListener test");
    }
}
```



### 发送与订阅延时消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
// 第一个参数 【DemoMsgDelayListener.class】为当前延时消息的处理实现类
// 第二个参数为延时消息体Object对象
// 第三个参数为延时时间，单位：秒
msgSender.sendDelayMessage(DemoMsgDelayListener.class,"922321333",5);
```

延时消息监听器实现类DemoMsgDelayListener.java

```java
/**
 * 订阅延时消息
 * 实现接口【MsgDelayListener】并设置回调消息body实体
 */
@Slf4j
@Component
public class DemoMsgDelayListener implements MsgDelayListener<String> {
    @Override
    public void onMessage(Message<String> message) {
        String body = message.getBody();
      log.info("接收消息: {}", message.getRequestId());
      // throw new RuntimeException("DemoMsgDelayListener test");
    }
}
```



### 异常捕获

当订阅消息或延时消息投递失败时，可以自定义消息重复投递次数和下次消息投递时间间隔（系统默认重复投递3次，每次间隔10秒），即便这样，消息还是有可能会存在投递不成功的问题，当消息进行最后一次投递还是失败时，可以使用注解`@Fail`
标识在消息处理类的接收方法或处理类上，到达最大重复投递次数且还是投递失败时调用此方法，即可捕获投递错误异常及数据。如下：

```java
/**
 * 订阅异步消息
 * 继承超类【MsgSubscribeListener】并设置监听的消息实体对象
 */
@Slf4j
@Component
public class DemoMsgSubscribeListener extends MsgSubscribeListener<String> {
    protected DemoMsgSubscribeListener() {
        // 订阅消息code
        super("testMsgSubscribe");
    }

    // 接收业务消息体对象数据
    // @Fail消息投递失败时重试，callMethod=投递失败时异常处理方法名，这里设置重试2次，下次重试间隔5秒后触发
    @Override
    @Fail(callMethod = "exceptionHandler", retry = 2, nextTime = 5)
    public void onMessage(Message<String> message) {
        String body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        // throw new RuntimeException("DemoMsgSubscribeListener test");
    }

    /**
     * 消息投递失败处理，参数顺序无要求
     */
    public void exceptionHandler(Message<String> message, Throwable throwable) {
        log.error("消息投递失败！: {}，{}", message.getRequestId(), throwable.getMessage());
    }
}
```

### 接口信息

| 接口                                                         | 说明                                                         | 示例                                                         |
| ------------------------------------------------------------ | :----------------------------------------------------------- | ------------------------------------------------------------ |
| [MsgSender](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgSender.java) | 消息的sender,用于消息的发送                                  | [DemoController ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/controller/DemoController.java) |
| [MsgSubscribeListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgSubscribeListener.java) | 接收广播消息的处理器接口类                                   | [DemoMsgSubscribeListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgSubscribeListener.java)<br/>[Demo2MsgSubscribeListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/Demo2MsgSubscribeListener.java) |
| [Subscribe ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Subscribe.java) | 接收广播消息处理器注解                                       | [DemoSubscribe ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoSubscribe.java)<br/>[Demo2Subscribe ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/Demo2Subscribe.java) |
| [MsgDelayListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgDelayListener.java) | 接收延时消息的处理器接口类                                   | [DemoMsgDelayListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgDelayListener.java) |
| [SubscribeDelay ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/SubscribeDelay.java) | 接收延时消息的处理器注解                                     | [DemoSubscribeDelay](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoSubscribeDelay.java) |
| [Fail ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Fail.java) | 接收消息处理投递失败时异常捕获注解                           | [DemoMsgSubscribeListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgSubscribeListener.java) |
| [SendBeforeInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendBeforeInterceptor.java) | 发送前全局拦截器                                             | [DemoSendBeforeInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendBeforeInterceptor.java) |
| [SendAfterInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendAfterInterceptor.java) | 发送后全局拦截器                                             | [DemoSendAfterInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendAfterInterceptor.java) |
| [DeliverSuccessInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverSuccessInterceptor.java) | 投递成功全局拦截器                                           | [DemoDeliverSuccessInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverSuccessInterceptor.java) |
| [DeliverThrowableInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverThrowableInterceptor.java) | 投递异常全局拦截器<br/> * 注：消息重复投递都失败时，最后一次消息投递失败时会调用该拦截器 | [DemoDeliverThrowableInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverThrowableInterceptor.java) |

更多信息请查阅相关接口类...

### 示例

启动springboot-demo访问http://localhost:8080/index.html

<img src="./doc/picture/event_send.jpg" alt="event_send" style="zoom: 33%; margin-left: 0px;" />

## 注意事项

**订阅、广播消息在消息引擎中是以订阅器实现类全类名加方法名进行分组（在rabbitMq中的存在是队列），当我们不在需要某个订阅器时请及时在消息引擎中删除此分组或队列，避免不必要的存储空间浪费。**

项目地址：[https://gitee.com/likavn/eventbus](https://gitee.com/likavn/eventbus)
