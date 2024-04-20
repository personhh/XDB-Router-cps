package com.cps.middleware.test;

import com.cps.middleware.db.router.annotation.DBRouter;

/**
 * @author cps
 * @description: TODO
 * @date 2024/3/20 16:30
 * @OtherDescription: Other things
 */
public interface IUserDao {

    @DBRouter(key = "userId")
    void insertUser(String req);
}
