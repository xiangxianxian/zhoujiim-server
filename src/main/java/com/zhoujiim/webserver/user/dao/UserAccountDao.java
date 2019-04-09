package com.zhoujiim.webserver.user.dao;
import java.util.Map;

import com.zhoujiim.webserver.base.dao.BaseDao;
import com.zhoujiim.webserver.user.model.UserAccountEntity;

/**
 * 用户帐号
 */
public interface UserAccountDao extends BaseDao<UserAccountEntity> {
	public UserAccountEntity queryObjectByAccount(Map<String, Object> map);
	public int update(UserAccountEntity userAccountEntity);
}
