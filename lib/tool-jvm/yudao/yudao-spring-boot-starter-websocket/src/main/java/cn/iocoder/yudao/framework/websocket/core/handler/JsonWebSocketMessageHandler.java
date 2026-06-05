package cn.iocoder.yudao.framework.websocket.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.websocket.core.listener.WebSocketMessageListener;
import cn.iocoder.yudao.framework.websocket.core.message.JsonWebSocketMessage;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JSON 格式 {@link WebSocketHandler} 实现类
 * <p>
 * 基于 {@link JsonWebSocketMessage#type} 消息类型，调度到对应的 {@link WebSocketMessageListener} 监听器。
 *
 * @author 芋道源码
 */
@Slf4j
public class JsonWebSocketMessageHandler extends TextWebSocketHandler {

    /**
     * type 与 WebSocketMessageListener 的映射
     */
    private final Map<String, WebSocketMessageListener<Object>> listeners = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonWebSocketMessageHandler(List<? extends WebSocketMessageListener> listenersList) {
        listenersList.forEach((Consumer<WebSocketMessageListener>)
                listener -> listeners.put(listener.getType(), listener));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 1.1 空消息，跳过
        if (message.getPayloadLength() == 0) {
            return;
        }
        // 1.2 ping 心跳消息，直接返回 pong 消息。
        if (message.getPayloadLength() == 4 && Objects.equals(message.getPayload(), "ping")) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 2.1 解析消息
        try {
            if (JSONUtil.isTypeJSON(message.getPayload())) {
                JSONObject jsonObject = JSONUtil.parseObj(message.getPayload());
                JSONObject body = jsonObject.getJSONObject("body");
                JSONArray dataInfos = body.getJSONArray("data_infos");
                JSONObject head = jsonObject.getJSONObject("head");
                if (!dataInfos.isEmpty() && "alarm".equals(head.getStr("type"))) {
                    log.info("[handleTextMessage][session({}) message({})]", session.getId(), message.getPayload());
                    this.sendCommand(session.getId(), head.getStr("type"));
                }
            } else {
                JsonWebSocketMessage jsonMessage = JsonUtils.parseObject(message.getPayload(), JsonWebSocketMessage.class);
                if (jsonMessage == null) {
                    log.error("[handleTextMessage][session({}) message({}) 解析为空]", session.getId(), message.getPayload());
                    return;
                }
                if (StrUtil.isEmpty(jsonMessage.getType())) {
                    log.error("[handleTextMessage][session({}) message({}) 类型为空]", session.getId(), message.getPayload());
                    return;
                }
                // 2.2 获得对应的 WebSocketMessageListener
                WebSocketMessageListener<Object> messageListener = listeners.get(jsonMessage.getType());
                if (messageListener == null) {
                    log.error("[handleTextMessage][session({}) message({}) 监听器为空]", session.getId(), message.getPayload());
                    return;
                }
                // 2.3 处理消息
                Type type = TypeUtil.getTypeArgument(messageListener.getClass(), 0);
                Object messageObj = JsonUtils.parseObject(jsonMessage.getContent(), type);
                Long tenantId = WebSocketFrameworkUtils.getTenantId(session);
                TenantUtils.execute(tenantId, () -> messageListener.onMessage(session, messageObj));
            }
        } catch (Throwable ex) {
            log.error("[handleTextMessage][session({}) message({}) 处理异常]", session.getId(), message.getPayload());
        }
    }

    /**
     * 消防系统发送命令
     */
    public void sendCommand(String sessionId, String command) {

        final String url = "http://192.168.31.133/prod-api/link/deviceInfo/control";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "736fc042-41ab-4f99-8edc-f18a2ebb52f6");
        JSONObject param = new JSONObject();
        param.putOpt("deviceName", "楼宇自控");
        param.putOpt("name", "指示灯2");
        param.putOpt("tagName", "DO2");
        param.putOpt("tagValue", "1");
        String post = HttpUtils.post(url, headers, JSONUtil.toJsonStr(param));
        log.info("send{}=>resp:{}",param, post);

        try {
            Thread.sleep(10000L);
            param.putOpt("tagValue", "0");
            String post1 = HttpUtils.post(url, headers, JSONUtil.toJsonStr(param));
            log.info("send{}=>resp:{}",param ,post1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
