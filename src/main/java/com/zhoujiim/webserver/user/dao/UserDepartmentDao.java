package com.zhoujiim.webserver.user.dao;
import java.util.List;

import com.zhoujiim.webserver.base.dao.BaseDao;
import com.zhoujiim.webserver.user.model.ImFriendUserData;
import com.zhoujiim.webserver.user.model.UserDepartmentEntity;

/**
 * 部门
 */
public interface UserDepartmentDao extends BaseDao<UserDepartmentEntity> {
	
	public List<ImFriendUserData> queryGroupAndUser(); 
}
