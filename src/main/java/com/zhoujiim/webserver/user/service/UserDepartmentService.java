package com.zhoujiim.webserver.user.service;

import java.util.List;
import java.util.Map;

import com.zhoujiim.webserver.user.model.ImFriendUserData;
import com.zhoujiim.webserver.user.model.UserDepartmentEntity;

/**
 * 部门
 * 
 */
public interface UserDepartmentService {
	
	UserDepartmentEntity queryObject(Long id);
	
	List<UserDepartmentEntity> queryList(Map<String, Object> map);
	
    List<ImFriendUserData> queryGroupAndUser(); 
	
	int queryTotal(Map<String, Object> map);
	
	void save(UserDepartmentEntity userDepartment);
	
	int update(UserDepartmentEntity userDepartment);
	
	int delete(Long id);
	
	int deleteBatch(Long[] ids);
}
