package com.mymemosproject.www.mymemos.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnChangeLisener;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;
import com.mymemosproject.www.mymemos.Activity.EditActivity;
import com.mymemosproject.www.mymemos.Activity.HomeActivity;
import com.mymemosproject.www.mymemos.Adapter.Memos_Item_Adapter;
import com.mymemosproject.www.mymemos.DB_Manager.Bean.List_Memos_Content;
import com.mymemosproject.www.mymemos.DB_Manager.Bean.Memos;
import com.mymemosproject.www.mymemos.R;
import com.mymemosproject.www.mymemos.Utils.XuListView;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class SomedayFragment extends Fragment {

    private XuListView listview;
    private View view;
    private BottomNavigationBar bottomNavigationBar;//底部导航栏
    private TextView title_top;

    private List<List_Memos_Content> mList;
    private List<Memos> memos1;
    private int to_do_num = 0;
    private int had_done_num = 0;
    private String someday_date;
    private String time;
    private TextView had_do_text;
    private TextView to_do_text;
    private Memos_Item_Adapter myAdapter;

    public static SomedayFragment newInstance() {
        SomedayFragment fragment = new SomedayFragment();
        //Bundle args = new Bundle();
        //args.putString("agrs1", param1);
        //fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_someday, container, false);
        //view.setVisibility(View.GONE);
        //LayoutInflater mInflater = LayoutInflater.from(getContext());
        //View contentView  = mInflater.inflate(R.layout.activity_home,null);
        HomeActivity homeActivity = (HomeActivity)getActivity();
        title_top = homeActivity.getTitle_top();
        bottomNavigationBar = homeActivity.getBottomNavigationBar();
        final DatePickDialog dialog = new DatePickDialog(getContext());
        //设置上下年分限制
        dialog.setYearLimt(5);
        //设置标题
        dialog.setTitle("选择时间");
        //设置类型
        dialog.setType(DateType.TYPE_YMD);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy-MM-dd");
        //设置选择回调
        dialog.setOnChangeLisener(new OnChangeLisener() {
            @Override
            public void onChanged(Date date) {

            }
        });
        //设置点击确定按钮回调
        dialog.setOnSureLisener(new OnSureLisener() {
            public void onSure(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                time = sdf.format(date);
                //((TextView) v).setText(time);
                setBottomNavigationItem(bottomNavigationBar, time);

                //Log.d("Sure", "onSure: "+time);
                setSomeday_date(time);
                listview = view.findViewById(R.id.memos_listview);
                to_do_num = 0;
                had_done_num = 0;
                initSomedayFragment();
                listview_addlistener();
            }
        });
        dialog.show();
/*        listview = view.findViewById(R.id.memos_listview);
        Log.d("mymeostest1234321", "onCreateView: ");
        to_do_num = 0;
        had_done_num = 0;
        initSomedayFragment();
        listview_addlistener();*/
        return view;
    }

    /*
      初始化SomedayFragment
    */
    public void initSomedayFragment() {
        setSomeday_date(someday_date);
        memos1 = new ArrayList<>();
        mList = new ArrayList<>();
        long today_text = 0;
        Date date_someday = null;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
       //Log.d("someday_text", "initSomedayFragment: "+someday_date);
        try {
            date_someday = format1.parse(someday_date);
            today_text = date_someday.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //String today_text = format1.format(today);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date_someday);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long day_next = 0;
        try {
            day_next = format1.parse(format1.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Log.d("MemosTest", String.valueOf(today_text) + " " +String.valueOf(day_next));
        //String next_day = format1.format(calendar.getTime());
        //Log.d("MemosTest", next_day);
        Cursor c = null;
        if(title_top.getText().toString().trim().equals("MyMemos: 工作")){
            c = LitePal.findBySQL("select * from Memos where create_date between ? and ? and category = ? ORDER BY create_date DESC", String.valueOf(today_text), String.valueOf(day_next), "工作");
            //Log.d("Mymemoswork", "initTodayFragment: ");
        }else if(title_top.getText().toString().trim().equals("MyMemos: 生活")){
            c = LitePal.findBySQL("select * from Memos where create_date between ? and ? and category = ? ORDER BY create_date DESC", String.valueOf(today_text), String.valueOf(day_next), "生活");
        }else if(title_top.getText().toString().trim().equals("MyMemos: 学习")){
            c = LitePal.findBySQL("select * from Memos where create_date between ? and ? and category = ? ORDER BY create_date DESC", String.valueOf(today_text), String.valueOf(day_next), "学习");
        }else{
            c = LitePal.findBySQL("select * from Memos where create_date between ? and ? ORDER BY create_date DESC", String.valueOf(today_text), String.valueOf(day_next));
        }
        if (c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex("title"));
                String date_str = c.getString(c.getColumnIndex("create_date"));
                SimpleDateFormat format;
                Date date = null;
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    date = format.parse(date_str);
                } catch (ParseException e) {
                    //Log.d("MemosTest", date_str);
                    Date dateOld = new Date(Long.valueOf(date_str));
                    String sDateTime = format.format(dateOld); // 把date类型的时间转换为string
                    try {
                        date = format.parse(sDateTime); // 把String类型转换为Date类型
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
                int state = c.getInt(c.getColumnIndex("state"));
                String isfinshed;
                if (state == 0) {
                    isfinshed = "未完成";
                    to_do_num++;
                }
                else {
                    isfinshed = "已完成";
                    had_done_num++;
                }
                String category = c.getString(c.getColumnIndex("category"));
                int ImageId = 0;
                if (category.equals("工作")) {
                    ImageId = R.drawable.work;
                } else if (category.equals("生活")) {
                    ImageId = R.drawable.life;
                } else if (category.equals("学习")) {
                    ImageId = R.drawable.study;
                }
                String html_memo_date = c.getString(c.getColumnIndex("html_memo_date"));
                Memos memos = new Memos(title, date, state, category, html_memo_date);
                List_Memos_Content list_memos_content = new List_Memos_Content(ImageId, title, format.format(date), html_memo_date, isfinshed);
                mList.add(list_memos_content);
                memos1.add(memos);
            } while (c.moveToNext());
        }

        had_do_text = view.findViewById(R.id.had_do_text);
        had_do_text.setText("已完成的事项：" + String.valueOf(had_done_num) + "项");

        to_do_text = view.findViewById(R.id.to_do_text);
        to_do_text.setText("未完成的事项：" + String.valueOf(to_do_num) + "项");

        if (memos1 != null) {
            for (List_Memos_Content memos : mList) {
/*                Log.d("MemosTest", "ImageId is " + memos.getImageId());
                Log.d("MemosTest", "Title is " + memos.getTitle());
                Log.d("MemosTest1", "State is " + memos.getIsfinished());
                //String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(memos.getCreate_date());
                Log.d("MemosTest", "Date is " + memos.getDate());
                Log.d("MemosTest", "Html is " + memos.getContent());*/
            }
        }
        /*List_Memos_Content_Adapter list_memos_content_adapter = new List_Memos_Content_Adapter(getActivity(), R.layout.view_list_item, memos_list);
        listview.setAdapter(list_memos_content_adapter);*/
        if(mList.size() > 0) {
            //Log.d("FirstItem", mList.get(0).getContent());
            //ListMemosContentAdapter myAdapter = new ListMemosContentAdapter();
            myAdapter = new Memos_Item_Adapter(getContext(), R.layout.view_list_item, mList);
            listview.setAdapter(myAdapter);
            listview.setClipToPadding(false);
            listview.setClipChildren(false);
            //滚动到中间
            listview.setSelection(myAdapter.getCount()/2
            );
        }
    }

    /*
    为Listview设置监听器
     */
    public void listview_addlistener(){
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeActivity.allContentActivity, EditActivity.class);
                Memos memos = null;
                if(memos1 != null) memos = memos1.get(position%memos1.size());
                intent.putExtra("memos_edit_data", memos);
                startActivity(intent);
                //Toast.makeText(getActivity(), ""+position, Toast.LENGTH_SHORT).show();
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           final int position, long id) {
                //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setMessage("确定删除?");
                builder.setTitle("提示");

                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Date date = null;
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        TextView date_view = null;
                        date_view = view.findViewById(R.id.date);
                        //Log.d("date_view", "onClick: "+date_view);
                        String date_text = null;
                        date_text = date_view.getText().toString().trim();
                        long _date = 0;
                        try {
                            date = formatter.parse(date_text);
                            _date = date.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        int m = LitePal.deleteAll(Memos.class, "create_date = ?", String.valueOf(_date));
                        int num = 0;
                        String str = to_do_text.getText().toString();
                        num = Integer.parseInt(str.substring(7, str.length() - 1));
                        int num1 = 0;
                        String str1 = had_do_text.getText().toString();
                        num1 = Integer.parseInt(str1.substring(7, str1.length() - 1));
                        if (null != mList && mList.size() > 0) {
                            //Log.d("Size_test", "onClick: "+mList.size());
                            Iterator it = mList.iterator();
                            while(it.hasNext()){
                                List_Memos_Content stu = (List_Memos_Content)it.next();
                                //Log.d("data_test", "onClick: "+stu.getDate()+" "+date_text);
                                if (stu.getDate().equals(date_text)) {
                                    if(stu.getIsfinished().equals("未完成")){
                                        num--;
                                        to_do_text.setText("未完成的事项：" + String.valueOf(num) + "项");
                                    }else{
                                        num1--;
                                        had_do_text.setText("已完成的事项：" + String.valueOf(num1) + "项");
                                    }
                                    it.remove(); //移除该对象
                                }
                            }
                        }
                        myAdapter.refreshData(mList);
                        myAdapter.notifyDataSetChanged();
                        listview.setSelection(myAdapter.getCount()/2);//滚到中间的位置

                        //myAdapter.notifyDataSetInvalidated();
                        Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                    }
                });

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
                return true;
            }
        });
    }


    public void setSomeday_date(String someday_date) {
        this.someday_date = someday_date;
    }

    /*
        通过反射修改底部导航栏的信息
     */
    private void setBottomNavigationItem(BottomNavigationBar bottomNavigationBar, String data_text){
        Class barClass = bottomNavigationBar.getClass();
        Field[] fields = barClass.getDeclaredFields();
        //Log.d("fieldstest", "setBottomNavigationItem: "+fields.length);
        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.getName().equals("mTabContainer")) {
                try {
                    //反射得到 mTabContainer
                    LinearLayout mTabContainer = (LinearLayout) field.get(bottomNavigationBar);
                    View view = mTabContainer.getChildAt(1);
                    TextView labelView = (TextView) view.findViewById(com.ashokvarma.bottomnavigation.R.id.fixed_bottom_navigation_title);
                    labelView.setText(data_text);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
