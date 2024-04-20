package com.cps.middleware.db.router.strategy;

/**
 * @author cps
 * @description: 路由策略
 * @date 2024/3/19 11:24
 * @OtherDescription: Other things
 */
public interface IDBRouterStrategy {

    /**
     * 路由计算
     * @param dbKeyArr 路由字段
     */
    void doRouter(String dbKeyArr);

    /**
     * 手动设置分库路由
     * @param dbIdx 路由库，需要在配置范围内
     */
    void setDBKey(int dbIdx);


    /**
     * 手动设置分表路由
     * @param tbIdx 路由表，需要在配置范围内
     */
    void setTBKey(int tbIdx);

    /**
     * 获取分库数
     * @return 数量
     */
    int dbCount();

    /**
     * 获取分表数
     * @return
     */
    int tbCount();

    /**
     * 清除路由
     */
    void clear();
}
