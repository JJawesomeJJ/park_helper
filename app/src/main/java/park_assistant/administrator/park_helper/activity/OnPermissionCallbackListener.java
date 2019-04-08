package park_assistant.administrator.park_helper.activity;

import java.util.List;

/**
 * 权限处理接口
 */
public interface OnPermissionCallbackListener {
    void onGranted();
    void onDenied(List<String> deniedPermissions);
}
