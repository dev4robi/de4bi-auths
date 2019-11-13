package com.authserver.aop;

import com.robi.data.ApiResult;
import com.robi.util.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Aspect
@Component
@AllArgsConstructor
public class ServiceAop {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.authserver.service..*.*(..))") // Service(SVC) AoP
    public Object aroundService(ProceedingJoinPoint pjp) {
        // Logger setting with issued 'tId' (A.K.A 'trace Id')
        final long beginTime = System.currentTimeMillis();
        final String oldLogLayer = LogUtil.changeLogLayer(LogUtil.LAYER_SVC);
        
        // Service method info
        Signature sign = pjp.getSignature();
        String packageName = sign.getDeclaringTypeName();
        String methodName = sign.getName();
        final String svcInfo = packageName + "." + methodName + "()";

        logger.info(">>> Service BGN! (svcInfo:'" + svcInfo + "')");

        Object svcRst = null;
        String svcRstInfo = null;

        try {
            svcRst = pjp.proceed(); // Service

            if (svcRst == null) {
                logger.error("Service returns null!");
                throw new Exception();
            }
            else if (svcRst instanceof ApiResult) {
                ApiResult svcApiRst = (ApiResult) svcRst;
                svcRstInfo = "result=" + svcApiRst.getResult() + ",msg=" + svcApiRst.getResultMsg();
            }
            else {
                logger.error("Service returns 'svcRst' type is NOT 'ApiResult'!");
                throw new Exception();
            }
        }
        catch (Throwable e) {
            logger.error("Service Exception!", e);
            return ApiResult.make(false, null, "서버 서비스 오류.");
        }

        logger.info(">>> Service END! (svcRstInfo:" + svcRstInfo + ", timeElapsed:" + (System.currentTimeMillis() - beginTime) + "ms)");
        LogUtil.changeLogLayer(oldLogLayer);
        return svcRst;
    }
}