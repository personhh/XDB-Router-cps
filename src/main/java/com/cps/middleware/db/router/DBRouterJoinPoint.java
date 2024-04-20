package com.cps.middleware.db.router;

import com.cps.middleware.db.router.annotation.DBRouter;
import com.cps.middleware.db.router.strategy.IDBRouterStrategy;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * @author cps
 * @description: TODO
 * @date 2024/3/19 10:57
 * @OtherDescription: Other things
 */
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    //数据路由配置
    private DBRouterConfig dbRouterConfig;

    //路由策略
    private IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    //切面点
    @Pointcut("@annotation(com.cps.middleware.db.router.annotation.DBRouter)")
    public void aopPoint(){

    }

    /**
     * 所有需要分库分表的操作，都需要使用自定义注解进行拦截，拦截后读取方法中的入参字段，根据字段进行路由操作。
     * 1. dbRouter.key() 确定根据哪个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取出对应的值。比如路由 key 是 uId，那么就从入参对象 Obj 中获取到 uId 的值。
     * 3. dbRouterStrategy.doRouter(dbKeyAttr) 路由策略根据具体的路由值进行处理
     * 4. 路由处理完成比，就是放行。 jp.proceed();
     * 5. 最后 dbRouterStrategy 需要执行 clear 因为这里用到了 ThreadLocal 需要手动清空。关于 ThreadLocal 内存泄漏介绍 https://t.zsxq.com/027QF2fae
     */
    //环绕方法
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable{
        //获取注解DBRouter中的key的值，判断是否为空
        String dbKey = dbRouter.key();
        if(StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())){
            throw new RuntimeException("annotation DBRouter key is null");
        }
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();

        //根据路由属性获取属性值，jp.getArgs()获得是切入点的属性对象（比如DBRouter中获得key）
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        //路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);
        //放行
        try{
            return jp.proceed();
        }finally {
            // 记得清理ThreadLocal
            dbRouterStrategy.clear();
        }
    }

    /**
     * 根据属性名获取属性值
     * @param attr 属性名
     * @param args 切入点中的方法所对应的属性对象
     * @return 属性值
     */
    private String getAttrValue(String attr, Object[] args){
        //如果属性集合只有一个属性就直接返回
        if(1 == args.length){
            Object arg = args[0];
            if(arg instanceof String){
                return arg.toString();
            }
        }

        String filedValue = null;
        //不只是一个属性，就循环查找
        for(Object arg : args){
            try{
                if(StringUtils.isNotBlank(filedValue)){
                    break;
                }
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            }catch (Exception e){
                logger.error("获取路由属性值失败 attr: {}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     * @param item 属性对象
     * @param name 属性名
     * @return 属性值
     */
    private Object getValueByName(Object item, String name){
        try{
            Field field = getFieldByName(item, name);
            if(field == null){
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        }catch (IllegalAccessException e){
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应的方法
     */
    private Field getFieldByName(Object item, String name){
        try{
            Field field;
            try{
                field = item.getClass().getDeclaredField(name);
            }catch (NoSuchFieldException e){
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        }catch (NoSuchFieldException e){
            return null;
        }
    }
}

