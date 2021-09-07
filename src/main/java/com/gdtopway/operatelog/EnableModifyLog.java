package com.gdtopway.operatelog;

import com.gdtopway.operatelog.parser.ContentParser;
import com.gdtopway.operatelog.parser.DefaultContentParse;
import com.gdtopway.operatelog.service.ILogsService;
import com.gdtopway.operatelog.util.ModifyName;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 记录编辑详细信息的标注
 * @author lw
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EnableModifyLog {
    /**
     * @return 操作的类型 可以直接调用ModifyName 不传时根据METHOD自动确定
     */
    ModifyName modifyType() default ModifyName.NONE;

    /**
     * @return 获取编辑信息的解析类，目前为使用id获取，复杂的解析需要自己实现，默认不填写
     *       则使用默认解析类
     */
    Class<? extends ContentParser> parseClass() default DefaultContentParse.class;

    /**
     * @return 查询数据库所调用的class文件
     */
    Class<? extends ILogsService> serviceClass() default ILogsService.class;

    /**
     * @return 具体业务操作名称
     */
    String handleName() default "";

    /**
     * @return 是否需要默认的改动比较
     */
    boolean needDefaultCompare() default false;

    /**
     * @return id的类型
     */
    Class<?> idType() default String.class;

    /*******************************************/
    /**
     * @return 模块类型（用工管理：1、员工自助：2），默认员工管理
     */
    String plateTypeName() default "1";

    /**
     * @return 业务功能（家庭关系、教育情况、专业技能……）
     */
    String businessFunctionName() default "";
}
