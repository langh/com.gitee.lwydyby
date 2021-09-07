package com.gdtopway.operatelog;


import java.lang.annotation.*;

/**
 * @author HL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DataName {
    /**
     * @return 字段名称
     */
    String name() default "";
    /**
     * @return 是否需要默认的改动比较  默认不比较
     */
    boolean ifCompare() default false;
    /**
     * @return 是否是附件  默认不是
     */
    boolean ifEnclosure() default false;
 }
