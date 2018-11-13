package com.jianye.smart.module.futureland;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.OkHttpUtils;
import com.movit.platform.common.okhttp.callback.StringCallback;
import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.module.workbench.activity.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/1/26.
 */
public class MagazineListActivity extends BaseActivity {

    private GridView gridView;
    private TextView topTitle;
    private ImageView topLeft, topRight,searchClear;
    private ProgressBar mProgressBar;
    private EditText searchText;
    private LinearLayout searchView;

    private MagazineAdapter adapter;
    private List<Magazine> list;
    private InputMethodManager inputmanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.futureland_activity_magazine_list);

        iniView();
        initSearchView();
        iniData();
    }

    private void iniView() {
        topTitle = (TextView) findViewById(R.id.tv_common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        gridView = (GridView) findViewById(R.id.magazine_gridview);

        mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
        mProgressBar.setMax(100);
    }

    private void initSearchView() {
        searchText = (EditText) findViewById(R.id.search_key);
        searchClear = (ImageView) findViewById(R.id.search_clear);
        searchView = (LinearLayout) findViewById(R.id.search_bar);
        searchView.setVisibility(View.GONE);

        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                searchData("");
            }
        });
        searchText.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchText.getText().toString();
                    if (content != null && !"".equals(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        searchView.setVisibility(View.VISIBLE);
                        searchData(content + "");
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                        searchView.setVisibility(View.GONE);
                    }
                    inputmanger.hideSoftInputFromWindow(
                            searchText.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void searchData(String content){

        String url = CommConstants.URL_STUDIO + "getMagazineListByTitleAndDate?searchContent="+content;
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new MyStringCallback());
    }

    private void iniData() {

        inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        topTitle.setText("新城内刊");
        topLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MagazineListActivity.this.finish();
            }
        });

        topRight.setImageDrawable(this.getResources().getDrawable(R.drawable.ico_search));
        topRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(View.VISIBLE == searchView.getVisibility()){
                    searchView.setVisibility(View.GONE);
                }else{
                    searchView.setVisibility(View.VISIBLE);
                }
            }
        });

        String url = CommConstants.URL_STUDIO + "getMagazineList";
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new MyStringCallback());
    }

    public class MyStringCallback extends StringCallback {
        @Override
        public void onBefore(Request request) {
            super.onBefore(request);
        }

        @Override
        public void onAfter() {
            super.onAfter();
        }

        @Override
        public void onError(Call call, Exception e) {
        }

        @Override
        public void onResponse(String response) {

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getBoolean("ok") && null != jsonObject.get("objValue")) {

                    list = new ArrayList<>();

                    JSONArray jsonArray = jsonObject.getJSONArray("objValue");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        Magazine magazine = new Magazine();
                        magazine.setId(jsonArray.getJSONObject(i).getString("id"));
                        magazine.setTitle(jsonArray.getJSONObject(i).getString("title"));
                        magazine.setDescription(jsonArray.getJSONObject(i).getString("description"));
                        magazine.setImage(jsonArray.getJSONObject(i).getString("image"));
                        magazine.setLink(jsonArray.getJSONObject(i).getString("link"));
                        magazine.setPublishDate(jsonArray.getJSONObject(i).getString("publishDate"));
                        list.add(magazine);
                    }

                    setAdapter();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void inProgress(float progress) {

            mProgressBar.setProgress((int) (100 * progress));
        }
    }

    private void setAdapter() {
        adapter = new MagazineAdapter(this, list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("URL", list.get(position).getLink());
                startActivity(intent);
            }
        });
    }
}
