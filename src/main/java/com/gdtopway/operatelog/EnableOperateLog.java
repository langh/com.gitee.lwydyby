package com.gdtopway.operatelog;

import com.gdtopway.operatelog.Interceptor.ModifyAspect;
import com.gdtopway.operatelog.parser.DefaultContentParse;
import com.gdtopway.operatelog.util.SpringUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author liwei
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({ModifyAspect.class, SpringUtil.class, DefaultContentParse.class})
public @interface EnableOperateLog {
}
