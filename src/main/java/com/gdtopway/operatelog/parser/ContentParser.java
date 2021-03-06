package com.gdtopway.operatelog.parser;

import com.gdtopway.operatelog.EnableModifyLog;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 解析接口
 *
 * @author lw
 */

public interface ContentParser {

    Logger logger = LoggerFactory.getLogger(ContentParser.class);

    /**
     * 获取信息返回查询出的对象
     *
     * @param joinPoint       查询条件的参数
     * @param enableModifyLog 注解
     * @return 获得的结果
     */
    Object getOldResult(JoinPoint joinPoint, EnableModifyLog enableModifyLog);

    /**
     * 获取信息返回查询出的对象
     *
     * @param joinPoint       查询条件的参数
     * @param enableModifyLog 注解
     * @return 获得的结果
     */
    Object getNewResult(JoinPoint joinPoint, EnableModifyLog enableModifyLog);

}

