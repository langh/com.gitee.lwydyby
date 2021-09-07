package com.gdtopway.operatelog.config;

import com.gdtopway.operatelog.Interceptor.ModifyAspect;
import com.gdtopway.operatelog.parser.DefaultContentParse;
import com.gdtopway.operatelog.util.SpringUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liwei
 */
@Configuration
@Import({ModifyAspect.class, SpringUtil.class, DefaultContentParse.class})
public class LogStarterAutoConfiguration {
}
