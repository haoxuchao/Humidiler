package com.wlp.humidifier;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {
    String commond_sd;
    String commond_wl;
    String commond_ds;
    String B;
    public static final String action ="com.example.broadcast";
    private  BroadcastReceiver broadcastReceiver;
    private static final String TAG ="ble_tag" ;
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;        //  添加右上方的菜单栏
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);
        ActivityCollector.addActivity(this);//每启动一个Activity，就将其加入到activity列表中

        TextView shidu = (TextView) findViewById(R.id.shidu);
        TextView wendu = (TextView) findViewById(R.id.wendu);
        TextView wuliang = (TextView) findViewById(R.id.wuliang);
        TextView shuiwei = (TextView) findViewById(R.id.shuiwei);


        Button sd = (Button) findViewById(R.id.sd);
        Button wl = (Button) findViewById(R.id.wl);
        Button dingshi = (Button) findViewById(R.id.dingshi);
        Button close = (Button) findViewById(R.id.tc);

        Spinner spinner_shidu = (Spinner)findViewById(R.id.spinner_shidu) ;
        Spinner spinner_wuliang = (Spinner)findViewById(R.id.spinner_wuliang) ;
        Spinner spinner_dingshi = (Spinner)findViewById(R.id.spinner_dingshi) ;


        /*************************  下拉菜单选项 湿度  ****************************************/

            //原始string数组
            final String[] spinnerItems = {"湿度 45%","湿度 55%","湿度 65%","湿度 75%","湿度 85%","湿度 95%"};
            //简单的string数组适配器：样式res，数组
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_spinner_item, spinnerItems);
            //下拉的样式res
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //绑定 Adapter到控件
            spinner_shidu.setAdapter(spinnerAdapter);
            //选择监听
            spinner_shidu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if("湿度 45%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="0301002024";
                    }
                    if("湿度 55%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="sd 55";
                    }
                    if("湿度 65%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="sd 65";
                    }
                    if("湿度 75%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="sd 75";
                    }
                     if("湿度 85%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="sd 85";
                    }
                    if("湿度 95%"== parent.getItemAtPosition(position).toString()){
                        commond_sd ="sd 95";
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
       /**************************  下拉菜单选项 雾量  ****************************************/

        //原始string数组
        final String[] spinnerItems_wl = {"雾量 小","雾量 中","雾量 大"};
        //简单的string数组适配器：样式res，数组
        ArrayAdapter<String> spinnerAdapter_wl = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerItems_wl);
        //下拉的样式res
        spinnerAdapter_wl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner_wuliang.setAdapter(spinnerAdapter_wl);
        //选择监听
        spinner_wuliang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if("雾量 小"== parent.getItemAtPosition(position).toString()){
                    commond_wl ="wl l";
                }
                if("雾量 中"== parent.getItemAtPosition(position).toString()){
                    commond_wl ="wl m";
                }
                if("雾量 大"== parent.getItemAtPosition(position).toString()){
                    commond_wl ="wl b";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
/**************************  下拉菜单选项 定时  ****************************************/

        //原始string数组
        final String[] spinnerItems_ds = {"0.5小时","1.0小时","1.5小时","2.0小时","3.0小时","5.0小时"};
        //简单的string数组适配器：样式res，数组
        ArrayAdapter<String> spinnerAdapter_ds = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerItems_ds);
        //下拉的样式res

        spinnerAdapter_ds.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner_dingshi.setAdapter(spinnerAdapter_ds);
        //选择监听
        spinner_dingshi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if("0.5小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds ="ds 0.5";
                }
                if("1.0小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds="ds 1.0";
                }
                if("1.5小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds ="ds 1.5";
                }
                if("2.0小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds ="ds 2.0";
                }
                if("3.0小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds ="ds 3.0";
                }
                if("5.0小时"== parent.getItemAtPosition(position).toString()){
                    commond_ds ="ds 5.0";
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /******************* 湿度  **********************************************/

        sd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(action);
                intent.putExtra("data", commond_sd);
                sendBroadcast(intent);
                Log.e(TAG,"湿度"+commond_sd);
            }
        });
        /******************* 雾量  **********************************************/

        wl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(action);
                intent.putExtra("data", commond_wl);
                sendBroadcast(intent);
                Log.e(TAG,"雾量"+commond_wl);
            }
        });
        /******************* 定时  **********************************************/

       dingshi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(action);
                intent.putExtra("data", commond_ds);
                sendBroadcast(intent);
                Log.e(TAG,"定时"+commond_ds);
            }
        });

        /*************************  关闭app****************************************/
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.finishAllActivity();

            }
        });
        /******************* 返回广播注册  **********************************************/
        IntentFilter infblue = new IntentFilter(SearchBtActivity.actionback);
        registerReceiver(receverback,infblue);

    }
    //接收蓝牙返回的数据  注册广播接收及销毁
    BroadcastReceiver receverback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intentback) {
            B = intentback.getStringExtra("data");
            Log.e(TAG,"接收到蓝牙向手机发的数据"+B);

        }

    };



    @Override      //注销广播
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receverback);
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessage(final String eventBus) {
    // 解析数据,填充数据
        ToastUtils.showLong(eventBus);
    }



    /******************* 右上角菜单*********************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item)     //重写菜单栏的点击事件
    {
        switch (item.getItemId()) {
            case R.id.add:                 //添加设备 方法
             Intent intent =new Intent(MainActivity.this,SearchBtActivity.class);
            startActivity(intent);
                break;
            case R.id.added:                 //已添加设备  方法

                break;
            case R.id.close:            //关闭设备  方法

                break;
            default:
        }
        return true;
    }
}
