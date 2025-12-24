package simplenews.cms.service;

import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.security.PermissionEnum;

import java.util.List;

public interface IPermissionService {
	List<PermissionEnum> getUserPermissions();

	boolean hasPermission(PermissionEnum permission);

	boolean canCreate(INewsDto newsDto);

	boolean canEdit(INewsDto newsDto);

	boolean canDelete(INewsDto newsDto);
}
