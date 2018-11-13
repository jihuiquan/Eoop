package com.jianye.smart.module.home.fragment;

import com.jianye.smart.module.workbench.model.WorkTable;
import java.util.List;

/**
 * @ClassName: HomeBean
 * @Description:
 * @Author: chao
 * @Data 2017-08-01 17:33
 */

class HomeBean {

  public List<DivisionBean> division_info;
  public List<WorkTable> type1;
  public List<WorkTable> type3;


  public static class DivisionBean {

    /**
     * id : 1
     * name : 集团总部
     * sort : 3
     * url : http://www.****.com
     */

    public String id;
    public String name;
    public int sort;
    public String url;
    public String isToken;
  }
}
