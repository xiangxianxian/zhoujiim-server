package com.zhoujiim.webserver.user.service;

import java.util.List;
import java.util.Map;

import com.zhoujiim.webserver.user.model.UserAccountEntity;

/**
 * 用户帐号
 * 
 */
public interface UserAccountService {
	
	UserAccountEntity queryObject(Long id);
	
	UserAccountEntity queryObjectByAccount(Map<String, Object> map);
	
	UserAccountEntity validateUser(Map<String, Object> map);
	
	List<UserAccountEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(UserAccountEntity userAccount);
	
	int update(UserAccountEntity userAccount);
	
	int delete(Long id);
	
	int deleteBatch(Long[] ids);
}
