![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-8+-green.svg) ![SpringBoot](https://img.shields.io/badge/Srping%20Boot-2.5+-green.svg) ![redis](https://img.shields.io/badge/Redis-6.2+-green.svg) ![rebbitmq](https://img.shields.io/badge/RabbitMQ-3.8.0+-green.svg)![rocketmq](https://img.shields.io/badge/RocketMQ-4.0+-green.svg)

## eventbus简介

eventbus是分布式业务消息分发总线组件，支持广播及时消息、延时消息等（即发布/订阅模式）。组件通过屏蔽底层不同种类的消息引擎，并提供统一的接口调用，可发送广播及时消息和延时消息，同时可订阅及时消息或延时消息等。当我们的应用引入eventbus组件时可降低系统耦合度。目前可选择基于Redis、RabbitMQ、RocketMQ等任意一种做底层的消息引擎，其他消息引擎中间件将被陆续支持。

注意：它不属于`消息中间件`，它是通过和消息中间件进行整合，来完成服务之间的消息通讯，类似于消息代理。

## 支持的消息中间件

- Redis
- RabbitMQ
- RocketMQ

## 有哪些特点

我们不是另外开发一个MQ，而是屏蔽底层不同种类的消息中间件，并提供统一的接口调用，旨在提供简单的事件处理编程模型，让基于事件的开发更灵活简单，结构清晰易于维护，扩展方便，集成使用更简单。

## 有哪些功能

- 消息：支持广播消息、延时消息的投递和接收，支持通过多种方式订阅消息，可通过统一的接口或注解方式去订阅接收消息；
- 重试：支持消息投递失败时投递重试，可自定义失败重试投递次数及下次投递时间；
- 拦截器：支持全局拦截器，可自主实现拦截逻辑，支持发送前拦截（`SendBeforeInterceptor `）、发送后拦截（`SendAfterInterceptor `
  ）、投递成功后拦截（`DeliverSuccessInterceptor `）、投递失败时拦截（`DeliverThrowableInterceptor `）；
- 提供消息持久化示例，可参考[BsHelper](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/service/BsHelper.java)，持久化消息投递状态，可便于后续处理；
- 可控的消息订阅监听器开关，如通过`Nacos`下线某个服务实例时需要同时关闭消息的监听；
- 消息中间件网络断开重连机制，支持重连；

## 有哪些场景可以使用

- 单一业务分发消息进行异步处理时，比如业务完成推送业务数据给第三方；
- 支付时，后端服务需要定时轮训支付接口查询是否支付成功；
- 系统业务消息传播解耦，降低消息投递和接收的复杂度（消息可靠性传递）；
- 当我们需要切换消息中间件时，可以做到无缝切换，不需要修改业务代码；

## 版本要求

1. SpringBoot 2.5.0.RELEASE+
2. Redis 6.2+
3. RabbitMQ 3.8.0+
4. RocketMQ 4.0+

## 快速开始

### 引入依赖

项目中必须引入`eventbus-spring-boot-starter`组件依赖

```xml
<!-- 必须引入 eventbus-spring-boot-starter组件-->
<dependency>
    <groupId>com.github.likavn</groupId>
    <artifactId>eventbus-spring-boot-starter</artifactId>
    <version>2.2.1</version>
</dependency>
```

`Json`序列化工具支持`Fast2json`、`Fastjson`、`Jackson`、`Gson`等任意一种。项目已引入`spring-boot-starter-web`时自带`Jackson`依赖，不需要单独引入其他`Json`工具依赖。如果项目中存在多个`Json`序列化工具依赖，序列化时的优先级如下:

```xml
<!-- 各JSON序列化工具 任选一个-->
<!-- fastjson2 -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>${version}</version>
</dependency>
<!-- fastjson -->
<dependency>
     <groupId>com.alibaba</groupId>
     <artifactId>fastjson</artifactId>
     <version>${version}</version>
</dependency>
<!-- jackson 如果项目已引入spring-boot-starter-web，项目自带jackson依赖，不需要单独引入-->
<dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>${version}</version>
</dependency>
<!-- gson -->
<dependency>
     <groupId>com.google.code.gson</groupId>
     <artifactId>gson</artifactId>
     <version>${version}</version>
</dependency>
```

### 设置消息中间件

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

rocketmq需要在pom.xml单独引入（rocketMQ会引入fastjson），如下：

```xml
<dependency>
     <groupId>org.apache.rocketmq</groupId>
     <artifactId>rocketmq-spring-boot-starter</artifactId>
     <version>2.1.1</version>
</dependency>
```

### 发送与订阅异步消息

发送消息

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
    }
}
```

也可让消息实体实现接口`MsgBody`，并可在实体中定义消息编码（可重写code方法，也可不重写，此时默认的消息编码code=TestBody （继承`MsgBody`接口的bean实体类名称）），使得同一类型的消息在定义监听器或发送消息时不需要单独设置消息编码。

```java
// 定义消息实体
@Data
public class TestBody implements MsgBody {
    private String content;

    // 可重新code方法，也可不重写，此时默认的消息编码code=TestBody （继承MsgBody接口的bean）
    @Override
    public String code() {
        return MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER;
    }
}
```

发送TestBody实体消息，如下：

```java
// 发送消息
@Resource
private MsgSender msgSender;

// 发送异步消息
TestBody testBody = new TestBody();
testBody.setContent("这是一个测试消息！！！");
// 第一个参数是业务消息Object实体对象数据
msgSender.send(testBody);
```

定义消息监听器（不需要指定消息编码），如下：

```java
@Slf4j
@Component
public class DemoMsgListener3 extends MsgListener<TestBody> {

    @Override
    public void onMessage(Message<TestBody> message) {
        TestBody body = message.getBody();
    }
}
```

也可基于注解[@Listener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Listener.java)的方式定义及时消息监听器，参考：[DemoAnnListener.java](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnListener.java)

### 发送与订阅延时消息

注意：当消息引擎为rocketMq时，延时时间为rocketMq的18个延时级别。

发送延时消息

```java
@Resource
private MsgSender msgSender;

// 发送异步消息
// 第一个参数 【DemoMsgDelayListener.class】为当前延时消息的处理实现类
// 第二个参数为延时消息体Object对象
// 第三个参数为延时时间，单位：秒 （当消息引擎为rocketMq时，延时时间为rocketMq的18个延时级别）
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
    }
}
```

也可基于注解[@DelayListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/DelayListener.java)的方式定义延时消息监听器，此时需定义延时消息编码，参考：[DemoAnnDelayListener.java](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnDelayListener.java)

### 异常捕获

当消息或延时消息投递失败时，可以自定义消息重复投递次数和下次消息投递时间间隔（系统默认重复投递3次，每次间隔10秒），即便这样，消息还是有可能会存在投递不成功的问题，当消息进行最后一次投递还是失败时，可以使用注解`@Fail`
标识在消息处理类的接收方法或处理类上，到达最大重复投递次数且还是投递失败时调用 `callMethod` 此方法，即可捕获投递错误异常及数据。如下：

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

## 高级使用

### 自定义消息ID

RequestId（消息ID）默认使用UUID，若需修改为其他类型的ID，可实现接口[RequestIdGenerator](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/RequestIdGenerator.java) 时重写`nextId()`方法并配置即可。

接口：

```java
public interface RequestIdGenerator {
    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    String nextId();
}
```

实现并配置：

```java
@Configuration
public class EventbusConfiguration {

    @Bean
    public RequestIdGenerator requestIdGenerator() throws UnknownHostException {
        Sequence sequence = new Sequence(InetAddress.getLocalHost());
        // RequestIdGenerator接口实现
        return () -> String.valueOf(sequence.nextId());
    }
}
```

### 自定义JSON序列化工具

当前`Json`序列化支持`Fast2json`、`Fastjson`、`Jackson`、`Gson`等任意一种，如果当前项目同时存在相关依赖时，序列化使用的`Json`工具优先级也同上顺序。若需调整顺序或使用其他`Json`序列化工具时，可以自定义`Json`实现，需实现接口[IJson](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/support/spi/IJson.java)。

接口：

```java
public interface IJson {
    /**
     * json工具类名称,如："com.alibaba.fastjson2.JSON"
     *
     * @return name
     */
    String className();

    /**
     * to json string
     *
     * @param value v
     * @return json str
     */
    String toJsonString(Object value);

    /**
     * json 转对象
     *
     * @param text text
     * @param type to bean class
     * @return bean
     */
    <T> T parseObject(String text, Type type);

    /**
     * 当存在多个可用的json工具时，优先使用order最小的
     *
     * @return order 顺序
     */
    int getOrder();
}
```

创建JsonProvider并实现接口IJson，需重写className、toJsonString、parseObject、getOrder等方法即可，可参考[Fast2jsonProvider](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/support/Fast2jsonProvider.java)的实现，实现代码如下：

```java
public class Fast2jsonProvider implements IJson {  
    @Override
    public String className() {
        return "com.alibaba.fastjson2.JSON";
    }

    @Override
    public String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public <T> T parseObject(String text, Type type) {
        return JSON.parseObject(text, type);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
```

这里使用的是Java的`SPI`机制，故需在项目的`META-INF/services`目录下添加`com.github.likavn.eventbus.core.support.spi.IJson`文件，文件内容如下：

```java
com.github.likavn.eventbus.core.support.Fast2jsonProvider
```

### 消息监听器开关

可控的消息监听器开关，如通过`Nacos`下线某个服务实例时需要同时关闭消息的监听。

```java
@Resource 
private MsgListenerContainer msgListenerContainer;

// 打开消息监听
msgListenerContainer.startup();

// 关闭消息监听
msgListenerContainer.shutdown();
```

### 全局消息拦截器

`eventbus`提供全局的消息拦截器，包含消息发送前拦截器、消息发送后拦截器、消息投递成功拦截器、消息投递失败时拦截器。可根据消息的重要性需求实现对应的拦截器接口，如对消息及消息的投递消费者状态进行数据库持久化操作，参考：[BsHelper](eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/service/BsHelper.java)。

#### 发送前拦截器

消息发送前拦截器：[SendBeforeInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendBeforeInterceptor.java)实现接口方法`execute`即可，如下示例是消息发送前持久化消息的实例代码，参考：[DemoSendBeforeInterceptor](eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendBeforeInterceptor.java)

```java
@Component
public class DemoSendBeforeInterceptor implements SendBeforeInterceptor {
  @Resource
  private BsHelper bsHelper;

  @Override
  public void execute(Request<String> request) {
    bsHelper.sendMessage(request);
  }
}
```

#### 发送后拦截器

消息发送后拦截器：[SendAfterInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendAfterInterceptor.java)实现接口方法`execute`即可。

#### 消费成功拦截器

消息投递消费者成功拦截器：[DeliverSuccessInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverSuccessInterceptor.java)实现接口方法`execute`即可，如下示例是消息消费成功后更新消息的投递状态示例代码，参考：[DemoDeliverSuccessInterceptor](eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverSuccessInterceptor.java)

```java
@Component
public class DemoDeliverSuccessInterceptor implements DeliverSuccessInterceptor {
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request) {
        bsHelper.deliverSuccess(request);
    }
}
```

#### 消费失败拦截器

消息投递消费者失败拦截器：[DeliverThrowableInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverThrowableInterceptor.java)实现接口方法`execute`即可， 如下示例是消息消费失败后更新消息的投递状态示例代码，参考：[DemoDeliverThrowableInterceptor](eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverThrowableInterceptor.java)

```java
@Component
public class DemoDeliverThrowableInterceptor implements DeliverThrowableInterceptor {
    @Resource
    private BsHelper bsHelper;

    @Override
    public void execute(Request<String> request, Throwable throwable) {
        bsHelper.deliverException(request, throwable);
    }
}
```

## 配置

`BusProperties`，在application.yaml中eventbus配置以 `eventbus` 开头，所有配置如下：


| 节点        | key                       | 数据类型 | 备注                                                                      |
| ----------- | ------------------------- | -------- | ------------------------------------------------------------------------- |
| eventbus    |                           |          | eventbus配置                                                              |
| eventbus    | serviceId                 | string   | 服务ID/消息来源ID,可以不用设置，默认等于spring.application.name           |
| eventbus    | type                      | string   | 消息引擎类别（redis、rabbitmq、rocketmq）                                 |
| eventbus    | concurrency               | int      | 定义异步消息接收并发级别，默认值为1。                                     |
| eventbus    | delayConcurrency          | int      | 定义接收延时消息并发级别，默认值为2。                                     |
| eventbus    | msgBatchSize              | int      | 单次获取消息数量，默认16条                                                |
| eventbus    | testConnect               |          | mq服务节点联通性配置                                                      |
| testConnect | pollSecond                | int      | 轮询检测时间间隔，单位：秒，默认：35秒进行检测一次                        |
| testConnect | loseConnectMaxMilliSecond | int      | 丢失连接最长时间大于等于次值设置监听容器为连接断开，单位：秒，默认：120秒 |
| eventbus    | fail                      |          | 消息投递失败时配置信息                                                    |
| fail        | retryCount                | int      | 消息投递失败时，一定时间内再次进行投递的次数，默认3次                     |
| fail        | nextTime                  | int      | 下次触发时间，单位：秒，默认10秒 ，（rocketMq的18个延时消息级别）         |
| eventbus    | redis                     |          | redis配置                                                                 |
| redis       | deliverTimeout            | int      | 消息超时时间，超时消息未被确认，才会被重新投递，默认5分钟。               |
| redis       | pendingMessagesBatchSize  | int      | 未确认消息，重新投递时每次最多拉取多少条待确认消息数据，默认：100条；     |
| redis       | streamExpiredHours        | int      | stream 过期时间，6.2及以上版本支持，单位：小时，默认 5 天                 |

## 接口信息


| 接口                                                                                                                                          | 说明                                                                                           | 示例                                                                                                                                                                                                                                                                  |
| --------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [MsgSender](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgSender.java)                                                 | 消息的生产者sender,用于消息的发送                                                              | [DemoController ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/controller/DemoController.java)                                                                                                                                       |
| [MsgListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgListener.java)                                             | 接收广播消息的处理器接口类                                                                     | [DemoMsgListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener.java)<br/>[DemoMsgListener2](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener2.java)  |
| [Listener ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Listener.java)                                           | 接收广播消息处理器注解                                                                         | [DemoAnnListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnListener.java)<br/>[DemoAnnListener2 ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnListener2.java) |
| [MsgDelayListener](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/MsgDelayListener.java)                                   | 接收延时消息的处理器接口类                                                                     | [DemoMsgDelayListener ](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgDelayListener.java)                                                                                                                             |
| [DelayListener ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/DelayListener.java)                                 | 接收延时消息的处理器注解                                                                       | [DemoAnnDelayListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoAnnDelayListener.java)                                                                                                                              |
| [Fail ](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/annotation/Fail.java)                                                   | 接收消息处理投递失败时异常捕获注解                                                             | [DemoMsgListener](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/listener/DemoMsgListener.java)                                                                                                                                        |
| [SendBeforeInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendBeforeInterceptor.java)             | 发送前全局拦截器                                                                               | [DemoSendBeforeInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendBeforeInterceptor.java)                                                                                                                 |
| [SendAfterInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/SendAfterInterceptor.java)               | 发送后全局拦截器                                                                               | [DemoSendAfterInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoSendAfterInterceptor.java)                                                                                                                   |
| [DeliverSuccessInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverSuccessInterceptor.java)     | 投递消费者成功全局拦截器                                                                       | [DemoDeliverSuccessInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverSuccessInterceptor.java)                                                                                                         |
| [DeliverThrowableInterceptor](./eventbus-core/src/main/java/com/github/likavn/eventbus/core/api/interceptor/DeliverThrowableInterceptor.java) | 投递消费者异常全局拦截器<br/> * 注：消息重复投递都失败时，最后一次消息投递失败时会调用该拦截器 | [DemoDeliverThrowableInterceptor](./eventbus-demo/springboot-demo/src/main/java/com/github/likavn/eventbus/demo/interceptor/DemoDeliverThrowableInterceptor.java)                                                                                                     |

更多信息请查阅相关接口类...

## 示例

示例项目需配置数据库，初始化数据库sql：[demo-init.sql](./doc/sql/demo-init.sql)

启动springboot-demo访问http://localhost:8080/index.html
<img src="./doc/picture/event_send.jpg" alt="event_send" style="zoom: 33%; margin-left: 0px;" />

## 注意事项

**订阅、广播消息在消息引擎中是以订阅器实现类全类名加方法名进行分组（在rabbitMq中的存在是队列），当我们不在需要某个订阅器时请及时在消息引擎中删除此分组或队列，避免不必要的存储空间浪费。**

Github项目地址：[https://github.com/likavn/eventbus](https://github.com/likavn/eventbus)
Gitee项目地址：[https://gitee.com/likavn/eventbus](https://gitee.com/likavn/eventbus)

## 联系我

本项目会持续更新和维护，喜欢别忘了 Star，有问题可通过微信、QQ及时联系我，谢谢您的关注。

微信：likavn

QQ：1085257460
