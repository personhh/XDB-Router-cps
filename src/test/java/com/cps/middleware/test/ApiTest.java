package com.cps.middleware.test;

import com.cps.middleware.db.router.DBContextHolder;
import com.cps.middleware.db.router.DBRouterConfig;
import com.cps.middleware.db.router.annotation.DBRouter;
import com.cps.middleware.db.router.strategy.Impl.DBRouterStrategyHashCode;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cps
 * @description: TODO
 * @date 2024/3/19 14:45
 * @OtherDescription: Other things
 */
public class ApiTest {

    @Test
    public void test_str_format(){
        System.out.println(String.format("db%02d", 1));
    }

    @Test
    public void test_Matcher_Pattern(){

        Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

        String sql  = "INSERT INTO user_strategy_export (u_id, activity_id) values(#{uId},#{activityId})";

        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if(matcher.find()){
            tableName = matcher.group().trim();
        }
        String replaceSql = matcher.replaceAll(tableName + "_" + "001");
        System.out.println(replaceSql);
    }

    @Test
    public void test_annotation() throws NoSuchMethodException {
        Class<IUserDao> iUserDaoClass = IUserDao.class;
        Method method = iUserDaoClass.getMethod("insertUser", String.class);
        DBRouter annotation = method.getAnnotation(DBRouter.class);
        System.out.println(annotation.key());
    }

    @Test
    public void test_db_hash(){
        String key = "suafavsdvsdgadgadawhkhftttt22223213ls";
        DBRouterConfig dbRouterConfig = new DBRouterConfig(2,5,key);

        DBRouterStrategyHashCode dbRouterStrategyHashCode = new DBRouterStrategyHashCode(dbRouterConfig);
        dbRouterStrategyHashCode.doRouter(key);
       /* int dbCount = 2;
        int tbCount = 4;
        int size = dbCount * tbCount;
        int idx = (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));
        int dbIdx = idx / tbCount + 1;
        int tbIdx  = idx - tbCount * (dbIdx - 1);
        System.out.println(dbIdx);
        System.out.println(tbIdx);*/
    }

}
