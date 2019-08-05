package com.authserver.aop;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.authserver.data.ApiResult;
import com.robi.util.LogUtil;
import com.robi.util.RandomUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.AllArgsConstructor;

@Aspect
@Component
@AllArgsConstructor
public class ControllerAop {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String KEYSTR_TID = "tid";

    @Around("execution(* com.authserver.controller.restcontroller..*.*(..))") // REST controller(CTR) AoP
    public Object aroundRestController(ProceedingJoinPoint pjp) {
        // Logger setting with issuing 'tId' (A.K.A 'trace Id')
        final long beginTime = System.currentTimeMillis();
        final String oldLogLayer = LogUtil.changeLogLayer(LogUtil.LAYER_CTR);
        final String tId = RandomUtil.genRandomStr(12, RandomUtil.ALPHABET | RandomUtil.NUMERIC);
        LogUtil.changeTid(tId);

        // Request info
        ServletRequestAttributes servletReqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletReqAttrs.getRequest();
        final String reqInfo = request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol();
        
        // Controller method info
        Signature sign = pjp.getSignature();
        String packageName = sign.getDeclaringTypeName();
        String methodName = sign.getName();
        final String ctrInfo = packageName + "." + methodName + "()";

        logger.info(">>> REST controller BGN! (reqInfo:'" + reqInfo + "', ctrInfo:'" + ctrInfo + "')");

        Object ctrRst = null;

        try {
            ctrRst = pjp.proceed(); // REST Controller

            if (ctrRst == null) {
                logger.error("Controller type is REST but, 'ctrRst' type is null!");
                throw new Exception();
            }
            else if (ctrRst instanceof Map<?,?>) {
                Map<String, Object> ctrRstMap = (Map) ctrRst;
                ctrRstMap.put(KEYSTR_TID, tId);
                ctrRst = ctrRstMap;
            }
            else {
                logger.error("Controller type is REST but, 'ctrRst' type is NOT 'Map'!");
                throw new Exception();
            }
        }
        catch (Throwable e) {
            logger.error("Controller Exception!", e);
            return ApiResult.make(false, "서버 컨트롤러 오류.");
        }

        logger.info(">>> REST controller END! (timeElapsed:" + (System.currentTimeMillis() - beginTime) + "ms)");
        LogUtil.changeLogLayer(oldLogLayer);
        return ctrRst;
    }
}