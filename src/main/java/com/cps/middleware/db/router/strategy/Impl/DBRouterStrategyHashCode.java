package com.cps.middleware.db.router.strategy.Impl;

import com.cps.middleware.db.router.DBContextHolder;
import com.cps.middleware.db.router.DBRouterConfig;
import com.cps.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author cps
 * @description: 哈希路由
 * @date 2024/3/19 11:28
 * @OtherDescription: Other things
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyArr) {
        //总表数
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        //扰动函数去找第几张表
        int idx = (size - 1) & (dbKeyArr.hashCode() ^ dbKeyArr.hashCode() >> 16);

        //计算出第几个库第几张表
        int dbIdx = idx /dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setDBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由 dbIdx: {}  tbIdx: {}", dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        //String.format() 是用来格式化，%d表示输出整数，%02d表示十进制，输出的数如果不足两位，用0补缺的一位
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));

    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
