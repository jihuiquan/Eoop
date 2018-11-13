package com.movit.platform.common.api;

public interface IUserManager {

  /**
   * @param userId 自己ID
   * @param otherId 其他ID
   */
  void recordAccessPersonInfo(final String userId, final String otherId);

}
