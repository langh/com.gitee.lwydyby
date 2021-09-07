package com.gdtopway.operatelog.Interceptor;

import com.gdtopway.operatelog.DataName;
import com.gdtopway.operatelog.EnableModifyLog;
import com.gdtopway.operatelog.entity.OperateLog;
import com.gdtopway.operatelog.parser.ContentParser;
import com.gdtopway.operatelog.service.OperatelogService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gdtopway.operatelog.util.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截@EnableGameleyLog注解的方法
 * 将具体修改存储到数据库中
 * Created by wwmxd on 2018/03/02.
 */
@Aspect
@Component
public class ModifyAspect {

    private final static Logger logger = LoggerFactory.getLogger(ModifyAspect.class);

    @Autowired
    private OperatelogService operatelogService;

    @Around("@annotation(enableModifyLog)")
    public Object around(ProceedingJoinPoint joinPoint, EnableModifyLog enableModifyLog) throws Throwable {
        Map<String, Object> oldMap = new HashMap<>();
        OperateLog operateLog = new OperateLog();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //当不传默认modifyType时 根据Method类型自动匹配
        setAnnotationType(request, enableModifyLog);
        // fixme 1.0.9开始不再提供自动存入username功能,请在存储实现类中自行存储
        operateLog.setModifyIp(ClientUtil.getClientIp(request));
        operateLog.setModifyDate(new Date());
//        operateLog.setMklx(enableModifyLog.plateTypeName());
        operateLog.setBusinessFunction(enableModifyLog.businessFunctionName());
        //如果不传,则默认存入请求的URI
        String handelName = enableModifyLog.handleName();
        if ("".equals(handelName)) {
            operateLog.setModifyObject(request.getRequestURL().toString());
        } else {
            operateLog.setModifyObject(handelName);
        }
        operateLog.setModifyName(enableModifyLog.modifyType());
        operateLog.setModifyContent("");

        if (ModifyName.UPDATE.equals(enableModifyLog.modifyType()) || ModifyName.DELETE.equals(enableModifyLog.modifyType())) {
            try {
                ContentParser contentParser = (ContentParser) SpringUtil.getBean(enableModifyLog.parseClass());
                Object oldObject = contentParser.getOldResult(joinPoint, enableModifyLog);
                operateLog.setOldObject(oldObject);
                Object ygbm = ReflectionUtils.getFieldValue(oldObject, "ygbm");
                Assert.notNull(ygbm, "未解析到ygbm值，请检查传递参数是否正确");
                Object ywid = ReflectionUtils.getFieldValue(oldObject, "id");
                Assert.notNull(ywid, "未解析到id值，请检查传递参数是否正确");
                operateLog.setYgbm(Long.valueOf(String.valueOf(ygbm)));
                operateLog.setYwid(Long.valueOf(String.valueOf(ywid)));
                //默认不进行比较，可以自己在logService中自定义实现，降低对性能的影响
                Object dataSource = ReflectionUtils.getFieldValue(oldObject, "dataSource");
                if(dataSource != null){
                    operateLog.setModifyType(Long.valueOf(String.valueOf(dataSource)));
                }
                if (enableModifyLog.needDefaultCompare() && ModifyName.UPDATE.equals(enableModifyLog.modifyType())) {
                    oldMap = (Map<String, Object>) objectToMap(oldObject);
                }
                //默认不进行比较，可以自己在logService中自定义实现，降低对性能的影响
                if (enableModifyLog.needDefaultCompare() && ModifyName.DELETE.equals(enableModifyLog.modifyType())) {
                    operateLog.setModifyContent(defaultDealSave(oldObject));
                }
            } catch (Exception e) {
                logger.error("service加载失败:", e);
            }
        }
        //执行service TODO 是否需要Catch Exception
        Object object = joinPoint.proceed();
        if (ModifyName.UPDATE.equals(enableModifyLog.modifyType()) || ModifyName.SAVE.equals(enableModifyLog.modifyType())) {
            ContentParser contentParser;
            try {
                contentParser = (ContentParser) SpringUtil.getBean(enableModifyLog.parseClass());
                object = contentParser.getNewResult(joinPoint, enableModifyLog);
                operateLog.setNewObject(object);
                Object ygbm = ReflectionUtils.getFieldValue(object, "ygbm");
                Assert.notNull(ygbm, "未解析到ygbm值，请检查传递参数是否正确");
                Object ywid = ReflectionUtils.getFieldValue(object, "id");
                Assert.notNull(ywid, "未解析到id值，请检查传递参数是否正确");
                Object dataSource = ReflectionUtils.getFieldValue(object, "dataSource");
                if(dataSource != null){
                    operateLog.setModifyType(Long.valueOf(String.valueOf(dataSource)));
                }
                operateLog.setYgbm(Long.valueOf(String.valueOf(ygbm)));
                operateLog.setYwid(Long.valueOf(String.valueOf(ywid)));
            } catch (Exception e) {
                logger.error("service加载失败:", e);
            }
            //默认不进行比较，可以自己在logService中自定义实现，降低对性能的影响
            if (enableModifyLog.needDefaultCompare() && ModifyName.UPDATE.equals(enableModifyLog.modifyType())) {
                operateLog.setModifyContent(defaultDealUpdate(object, oldMap));
            }
            //默认不进行比较，可以自己在logService中自定义实现，降低对性能的影响
            if (enableModifyLog.needDefaultCompare() && ModifyName.SAVE.equals(enableModifyLog.modifyType())) {
                operateLog.setModifyContent(defaultDealSave(object));
            }
        } else {
            //除了更新外,默认把返回的对象存储到log中
            operateLog.setNewObject(object);
        }
        return operatelogService.getOperatelog(operateLog);
    }

    private String defaultDealUpdate(Object newObject, Map<String, Object> oldMap) {
        try {
            Map<String, Object> newMap = (Map<String, Object>) objectToMap(newObject);
            StringBuilder str = new StringBuilder();
            Object finalNewObject = newObject;
            oldMap.forEach((k, v) -> {
                Object newResult = newMap.get(k);
                boolean existed_1 = v != null && !"".equals(String.valueOf(v)) && !v.equals(newResult);
                boolean existed_2 = newResult != null && !"".equals(String.valueOf(newResult)) && !(String.valueOf(v)).equals(String.valueOf(newResult));
                if (existed_1 || existed_2) {
                    Field field = ReflectionUtils.getAccessibleField(finalNewObject, k);
                    DataName dataName = field.getAnnotation(DataName.class);
                    if (dataName != null && dataName.ifCompare()) {
                        str.append("【").append(dataName.name()).append("】从【")
                                .append(
                                        v != null ? (dataName.ifEnclosure() ? CommonConstants.ENCLOSURE_KEY_STARTNAME + v + CommonConstants.ENCLOSURE_KEY_STARTNAME : v+"") : "")
                                .append("】改为了【").append(
                                        newResult != null ? (dataName.ifEnclosure() ? CommonConstants.ENCLOSURE_KEY_ENDNAME + newResult + CommonConstants.ENCLOSURE_KEY_ENDNAME : newResult + "") : ""
                        ).append("】;\n");
                    }
                }

            });
            return str.toString();
        } catch (Exception e) {
            logger.error("比较异常", e);
            throw new RuntimeException("比较异常", e);
        }
    }

    private String defaultDealSave(Object newObject) {
        try {
            Map<String, Object> newMap = (Map<String, Object>) objectToMap(newObject);
            StringBuilder str = new StringBuilder();
            Object finalNewObject = newObject;
            newMap.forEach((k, v) -> {
                if (v != null && !"".equals(v)) {
                    Field field = ReflectionUtils.getAccessibleField(finalNewObject, k);
                    DataName dataName = field.getAnnotation(DataName.class);
                    if (dataName != null && dataName.ifCompare()) {
                        str.append("【").append(dataName.name()).append("】为【")
                                .append(dataName.ifEnclosure() ? CommonConstants.ENCLOSURE_KEY_STARTNAME + v + CommonConstants.ENCLOSURE_KEY_STARTNAME : v ).append("】;\n");
                    }
                }
            });
            return str.toString();
        } catch (Exception e) {
            logger.error("比较异常", e);
            throw new RuntimeException("比较异常", e);
        }
    }

    private Map<?, ?> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //如果使用JPA请自己打开这条配置
        //mapper.addMixIn(Object.class, IgnoreHibernatePropertiesInJackson.class);
        Map<?, ?> mappedObject = mapper.convertValue(obj, Map.class);

        return mappedObject;
    }

    private void setAnnotationType(HttpServletRequest request, EnableModifyLog modifyLog) {
        if (!modifyLog.modifyType().equals(ModifyName.NONE)) {
            return;
        }
        String method = request.getMethod();
        if (RequestMethod.GET.name().equalsIgnoreCase(method)) {
            ReflectAnnotationUtil.updateValue(modifyLog, "modifyType", ModifyName.GET);
        } else if (RequestMethod.POST.name().equalsIgnoreCase(method)) {
            ReflectAnnotationUtil.updateValue(modifyLog, "modifyType", ModifyName.SAVE);
        } else if (RequestMethod.PUT.name().equalsIgnoreCase(method)) {
            ReflectAnnotationUtil.updateValue(modifyLog, "modifyType", ModifyName.UPDATE);
        } else if (RequestMethod.DELETE.name().equalsIgnoreCase(method)) {
            ReflectAnnotationUtil.updateValue(modifyLog, "modifyType", ModifyName.DELETE);
        }

    }

}
