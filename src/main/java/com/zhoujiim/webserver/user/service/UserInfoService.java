package com.zhoujiim.webserver.user.service;

import java.util.List;
import java.util.Map;

import com.zhoujiim.webserver.user.model.UserInfoEntity;

/**
 * 用户信息表
 * 
 */
public interface UserInfoService {
	
	UserInfoEntity queryObject(Long id);
	
	List<UserInfoEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(UserInfoEntity userInfo);
	
	int update(UserInfoEntity userInfo);
	
	int delete(Long id);
	
	int deleteBatch(Long[] ids);
}
