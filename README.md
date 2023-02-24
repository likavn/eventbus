## 什么是 notify-spring-boot-starter？

notify-spring-boot-starter消息组件，支持分布式业务消息总线、延时消息等，屏蔽底层消息引擎种类，提供线上统一的接口，可发送异步消息及延时消息，同时可订阅异步消息或延时消息。目前可选择基于redis、rabbitmq等任一一种做消息引擎，其他消息中间件将陆续支持。



### 支持的消息引擎中间件

- redis
- rabbitmq



## 有哪些特点？

屏蔽底层不同的中间件类型，统一接口调用。



## 有哪些功能？

异步业务消息订阅及延时消息订阅



## 有哪些场景可以使用？

单一业务分发消息进行异步处理时，比如业务完成推送业务数据给第三方；

支付时，后端服务需要定时轮训支付接口查询是否支付成功；



## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>notify-spring-boot-starter</artifactId>
    <version>1.3.3</version>
</dependency>
```



### 设置消息引擎类别

在application.yml文件中配置消息引擎类别，如下：

```yaml
notify:
  type: redis  #redis或者rabbitmq
```

如果是redis，需要在pom.xml单独引入，如下：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

如果是rabbitmq，需要在pom.xml单独引入，如下：

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

更多信息请查阅相关接口类...

项目地址：[https://github.com/likavn/notify-spring-boot-starter](https://github.com/likavn/notify-spring-boot-starter)

