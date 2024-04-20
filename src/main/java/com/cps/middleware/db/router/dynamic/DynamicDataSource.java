package com.cps.middleware.db.router.dynamic;

import com.cps.middleware.db.router.DBContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author cps
 * @description: 动态数据源获取，每当切换数据源,都要从这里进行获取
 * @date 2024/3/19 17:01
 * @OtherDescription: Other things
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Value("${mini-db-router.jdbc.datasource.default}")
    private String defaultDataSource;
    @Override
    protected Object determineCurrentLookupKey() {
        if(null == DBContextHolder.getDBKey()){
            return defaultDataSource;
        }else{
            return "db" + DBContextHolder.getDBKey();
        }
    }
}
