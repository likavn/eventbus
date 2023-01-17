## 什么是 notify-spring-boot-starter？

​ 分布式业务消息总线、延时消息等，屏蔽底层消息引擎类别，统一线上接口调用，可订阅异步消息及延时消息。目前可选择基于redis、rabbitmq等任一一种做消息引擎。

## 有哪些特点？

​ 屏蔽底层不同的中间件类型，统一接口调用。

## 有哪些功能？

 	异步业务消息订阅及延时消息订阅

## 快速开始

### 引入依赖

```xml

<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>notify-spring-boot-starter</artifactId>
    <version>1.1.3</version>
</dependency>
```

###  

#### 发送订阅异步消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
        msgSender.send("testMsgSubscribe","charging");
```

接收异步消息

```java
/**
 * 继承超类「SubscribeMsgListener」设置监听的消息实体
 */
@Slf4j
@Component
public class ChargingOrderListener extends SubscribeMsgListener<String> {

    public ChargingOrderListener() {
        // 设置订阅的消息类型，其他类型的服务的消息类型，可设置对应服务的id+业务消息类型
        super(Collections.singletonList("testMsgSubscribe"));
    }

    /**
     * 接收消息体
     */
    @Override
    public void accept(String msg) {
        log.info("消息监听：{}", msg);
    }
}
```

#### 发送订阅延时消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息，DelayMsgDemoListener，为当前延时消息的处理的实现类
        msgSender.sendDelayMessage(DelayMsgDemoListener.class,"922321333",5);
```

订阅延时消息

```java
/**
 * 实现接口[DelayMsgListener]并设置回调body实体
 */
@Slf4j
@Component
public class DelayMsgDemoListener implements DelayMsgListener<String> {
    @Override
    public void onMessage(MsgRequest<String> msg) {
        log.info("接收延时消息回调body:{}", msg.getBody());
    }
}
```

更多信息请查阅接口类...