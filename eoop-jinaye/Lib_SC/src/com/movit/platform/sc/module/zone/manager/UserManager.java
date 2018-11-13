package com.movit.platform.sc.module.zone.manager;

import android.content.Context;
import com.movit.platform.common.api.IUserManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.manager.HttpManager;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: IUserManager
 * @Description:
 * @Author: chao
 * @Data 2017-08-14 19:26
 */
public class UserManager implements IUserManager {

  private static UserManager manager;
  private Context context;

  private UserManager(Context context) {
    super();
    this.context = context;
  }

  public static UserManager getInstance(Context mContext) {
    if (manager == null) {
      manager = new UserManager(mContext);
    }
    return manager;
  }

  /**
   * @param userId 自己ID
   * @param otherId 其他ID
   */
  public void recordAccessPersonInfo(final String userId, final String otherId) {
    String url = "http://gzt.jianye.com.cn:80/eoop-api/r/recordAccessPersonInfo";
    JSONObject object = new JSONObject();
    try {
      object.put("accessUserId", userId);
      object.put("visitedUserId", otherId);
      HttpManager.postJson(url, object.toString(), new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
        }

        @Override
        public void onResponse(String response) throws JSONException {
        }
      });
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
