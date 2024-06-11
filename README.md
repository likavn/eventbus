![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-8+-green.svg) ![SpringBoot](https://img.shields.io/badge/Srping%20Boot-2.5+-green.svg) ![redis](https://img.shields.io/badge/Redis-6.2+-green.svg) ![rebbitmq](https://img.shields.io/badge/RabbitMQ-3.8.0+-green.svg)![rocketmq](https://img.shields.io/badge/RocketMQ-4.0+-green.svg)

## eventbus是什么

eventbus是分布式业务消息分发总线组件，支持广播及时消息、延时消息等。组件通过屏蔽底层不同种类的消息引擎，并提供统一的接口调用，可发送广播及时消息和延时消息，同时可订阅及时消息或延时消息等。当我们的应用引入eventbus组件时有利于降低系统耦合度。目前可选择基于Redis、RabbitMQ、RocketMQ等任一 一种做底层的消息引擎，其他消息引擎中间件将被陆续支持。

注意：它不属于`消息中间件`，他是通过和消息中间件整合，来完成服务之间的消息通讯，类似于消息代理。

### 支持的消息引擎中间件

- Redis
- RabbitMQ
- RocketMQ

计划支持Pulsar

## 有哪些特点

我们不是另外开发一个MQ，而是屏蔽底层不同种类的消息中间件，并提供统一的接口调用。

## 有哪些功能

- 消息：支持广播消息、延时消息的投递和接收，可通过统一的接口或注解方式去订阅接收消息；
- 重试：支持消息投递失败时投递重试，可自定义失败重试投递次数及下次投递时间；
- 拦截器：支持全局拦截器，可自主实现拦截逻辑，支持发送前拦截（`SendBeforeInterceptor `）、发送后拦截（`SendAfterInterceptor `
  ）、投递成功后拦截（`DeliverSuccessInterceptor `）、投递失败时拦截（`DeliverThrowableInterceptor `）；

## 有哪些场景可以使用

单一业务分发消息进行异步处理时，比如业务完成推送业务数据给第三方；

支付时，后端服务需要定时轮训支付接口查询是否支付成功；

系统业务消息传播解耦，降低消息投递和接收的复杂度；

## 版本要求

1.SpringBoot 2.5.0.RELEASE+

2.Redis 6.2+

3.RabbitMQ 3.8.0+

4.RocketMQ 4.0+

## 快速开始

### 引入依赖

json序列化支持`Fast2json`、`Fastjson`、`Jackson`、`Gson`等任意一种，如果存在多个json序列化工具依赖，序列化时的优先级如上。

```xml
<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>eventbus-spring-boot-starter</artifactId>
    <version>2.2</version>
</dependency>
<!-- 各JSON序列化工具 任选一个-->
<!-- fastjson2 -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
<!-- fastjson -->
<dependency>
     <groupId>com.alibaba</groupId>
     <artifactId>fastjson</artifactId>
     <version>${fastjson.version}</version>
</dependency>
<!-- jackson 如果项目已引入spring-boot-starter-web，项目自带jackson依赖，不需要单独引入-->
<dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>${jackson.version}</version>
</dependency>
<!-- gson -->
<dependency>
     <groupId>com.google.code.gson</groupId>
     <artifactId>gson</artifactId>
     <version>${gson.version}</version>
</dependency>
```

### 设置消息引擎类别

在application.yml文件中配置消息引擎类型，如下：

```yaml
eventbus:
  type: redis  #redis或者rabbitmq、rocketmq
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

rabbitmq需要在pom.xml单独引入，如下：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

#### RocketMQ

rocketmq需要在pom.xml单独引入，如下：

```xml
<dependency>
     <groupId>org.apache.rocketmq</groupId>
     <artifactId>rocketmq-spring-boot-starter</artifactId>
     <version>2.1.1</version>
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

订阅异步业务消息监听器实现类[DemoMsgListener.java](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener.java)

```java
/**
 * 订阅异步消息
 * 继承超类【MsgListener】并设置监听的消息实体对象
 */
@Slf4j
@Component
public class DemoMsgListener extends MsgListener<String> {
    protected DemoMsgListener() {
        // 订阅消息code
        super("testMsgSubscribe");
    }

    // 接收业务消息体对象数据
    @Override
    public void onMessage(Message<String> message) {
        String body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        // throw new RuntimeException("DemoMsgListener test");
    }
}
```

也可使消息实体实现接口`MsgBody`，并在实体中定义消息编码，使得同一类型的消息在定义监听器或发送消息时不需要单独设置消息编码。

```java
// 定义消息实体@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestBody implements MsgBody {
    private String content;

    @Override
    public String code() {
        return MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER;
    }
}

// 发送消息
@Resource
private MsgSender msgSender;

// 发送异步消息
TestBody testBody = new TestBody();
testBody.setContent("这是一个测试消息！！！");
// 第一个参数是业务消息Object实体对象数据
msgSender.send(testBody);

```

定义消息监听器

```java
@Slf4j
@Component
public class DemoMsgListener3 extends MsgListener<TestBody> {

    @Override
    public void onMessage(Message<TestBody> message) {
        TestBody body = message.getBody();
        log.info("接收数据: {}", message.getRequestId());
        //throw new RuntimeException("DemoMsgListener3 test");
    }
}
```

### 发送与订阅延时消息

注意：当消息引擎为rocketMq时，延时时间为rocketMq的18个延时级别。

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
// 第一个参数 【DemoMsgDelayListener.class】为当前延时消息的处理实现类
// 第二个参数为延时消息体Object对象
// 第三个参数为延时时间，单位：秒 
msgSender.sendDelayMessage(DemoMsgDelayListener.class,"922321333",5);
```

延时消息监听器实现类[DemoMsgDelayListener.java](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgDelayListener.java)

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
public class DemoMsgSubscribeListener extends MsgListener<String> {
    protected DemoMsgSubscribeListener() {
        // 订阅消息code
        super("testMsgSubscribe");
    }

    // 接收业务消息体对象数据
    // @Fail消息投递失败时重试，callMethod=投递失败时异常处理方法名，这里设置重试2次，下次重试间隔5秒（引擎为rocketMq时，此处延时时间为rocketMq的18个延时级别）后触发
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

## 接口信息


| 接口                                                                                                                                          | 说明                                                                                     | 示例                                                                                                                                                                                                                                                                                                     |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [MsgSender](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgSender.java)                                                 | 消息的sender,用于消息的发送                                                              | [DemoController ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/controller/DemoController.java)                                                                                                                                                                          |
| [MsgListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgListener.java)                                             | 接收广播消息的处理器接口类                                                               | [DemoMsgListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener.java)<br/>[DemoMsgListener2](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener2.java) |
| [Listener ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Listener.java)                                           | 接收广播消息处理器注解                                                                   | [DemoAnnListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnListener.java)<br/>[DemoAnnListener2 ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnListener2.java)                                            |
| [MsgDelayListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgDelayListener.java)                                   | 接收延时消息的处理器接口类                                                               | [DemoMsgDelayListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgDelayListener.java)                                                                                                                                                                |
| [DelayListener ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/DelayListener.java)                                 | 接收延时消息的处理器注解                                                                 | [DemoAnnDelayListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnDelayListener.java)                                                                                                                                                                     |
| [Fail ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Fail.java)                                                   | 接收消息处理投递失败时异常捕获注解                                                       | [DemoMsgListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener.java)                                                                                                                                                         |
| [SendBeforeInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendBeforeInterceptor.java)             | 发送前全局拦截器                                                                         | [DemoSendBeforeInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendBeforeInterceptor.java)                                                                                                                                                    |
| [SendAfterInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendAfterInterceptor.java)               | 发送后全局拦截器                                                                         | [DemoSendAfterInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendAfterInterceptor.java)                                                                                                                                                      |
| [DeliverSuccessInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverSuccessInterceptor.java)     | 投递成功全局拦截器                                                                       | [DemoDeliverSuccessInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverSuccessInterceptor.java)                                                                                                                                            |
| [DeliverThrowableInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverThrowableInterceptor.java) | 投递异常全局拦截器<br/> * 注：消息重复投递都失败时，最后一次消息投递失败时会调用该拦截器 | [DemoDeliverThrowableInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverThrowableInterceptor.java)                                                                                                                                        |

更多信息请查阅相关接口类...

## 示例

启动springboot-demo访问http://localhost:8080/index.html

<img src="./doc/picture/event_send.jpg" alt="event_send" style="zoom: 33%; margin-left: 0px;" />

## 注意事项

**订阅、广播消息在消息引擎中是以订阅器实现类全类名加方法名进行分组（在rabbitMq中的存在是队列），当我们不在需要某个订阅器时请及时在消息引擎中删除此分组或队列，避免不必要的存储空间浪费。**

项目地址：[https://gitee.com/likavn/eventbus](https://gitee.com/likavn/eventbus)
