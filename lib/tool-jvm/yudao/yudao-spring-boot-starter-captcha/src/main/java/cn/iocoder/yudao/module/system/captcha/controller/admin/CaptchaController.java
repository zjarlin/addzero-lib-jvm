package cn.iocoder.yudao.module.system.captcha.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;

@RestController("adminCaptchaController")
@RequestMapping("/system/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data) {
        data.setBrowserInfo(getRemoteId());
        return captchaService.get(data);
    }

    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data) {
        data.setBrowserInfo(getRemoteId());
        return captchaService.check(data);
    }

    private static String getRemoteId() {
        Object request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String ip = firstNonBlank(
                getHeader(request, "x-forwarded-for"),
                getHeader(request, "X-Forwarded-For"),
                getHeader(request, "X-Real-IP"),
                invokeString(request, "getRemoteAddr")
        );
        if (StrUtil.isBlank(ip)) {
            return null;
        }
        if (ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(',')).trim();
        }
        return ip + StrUtil.nullToEmpty(getHeader(request, "user-agent"));
    }

    private static Object getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        try {
            Method method = attributes.getClass().getMethod("getRequest");
            return method.invoke(attributes);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static String getHeader(Object request, String name) {
        try {
            Method method = request.getClass().getMethod("getHeader", String.class);
            Object value = method.invoke(request, name);
            return value instanceof String ? (String) value : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static String invokeString(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof String ? (String) value : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value) && !"unknown".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return null;
    }

}
