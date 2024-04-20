package com.cps.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author cps
 * @description: 路由策略，分表标记
 * @date 2024/3/19 11:08
 * @OtherDescription: Other things
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    /**
     * 判断是否分表
     * @return 是否分表
     */
    boolean splitTable() default false;
}
