package com.cps.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author cps
 * @description: 路由注解
 * @date 2024/3/19 11:06
 * @OtherDescription: Other things
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface DBRouter {

    /** 分库分表字段 */
    String key() default "";
}
