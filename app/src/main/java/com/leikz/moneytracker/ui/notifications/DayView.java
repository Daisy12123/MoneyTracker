package com.leikz.moneytracker.ui.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.leikz.moneytracker.R;
import com.leikz.moneytracker.database.DatabaseAction;
import com.leikz.moneytracker.database.MyDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;
//import lecho.lib.hellocharts.listener.PieChartOnValueTouchListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;

public class DayView extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int COMPLETED = -1;
    private PieChartView pieChart;     //饼状图View
    private PieChartData data;         //存放数据
    private TextView im;       //收入
    private TextView om;       //支出
    private TextView am;       //净收入
    private List<MyDatabase> db;
    List<SliceValue> values;
    private long i;
    private long o;

    public DayView() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment1.
     */
    // TODO: Rename and change types and number of parameters
//    创建DayView实例，并传递参数
    public static DayView newInstance(String param1, String param2) {
        DayView fragment = new DayView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    获取传递的参数
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

//    设置视图和处理日期选择
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.day_view, container, false);
        pieChart = root.findViewById(R.id.pie_chart);
        im = root.findViewById(R.id.inM);
        om = root.findViewById(R.id.outM);
        am = root.findViewById(R.id.allM);
        pieChart.setChartRotationEnabled(false);
        Calendar calendar = Calendar.getInstance();
        update(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        CalendarView calendarView = root.findViewById(R.id.calendarView);
        calendarView.setDate(calendar.getTimeInMillis());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                update(year, month + 1, dayOfMonth);
            }
        });

        pieChart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                // 获取选中的饼图数据
                MyDatabase database = db.get(arcIndex);
                // 获取类别、金额和百分比
                String category = database.getType();
                String amount = String.format("%.2f", (double) database.getMoney() / 100);
                float percentage = (value.getValue() / getTotalValue()) * 100;
                // 在这里你可以自定义如何显示这些信息，例如弹出一个对话框或者更新其他的UI元素
                Toast.makeText(getContext(), "类别: " + category + "\n金额: " + amount + "\n百分比: " + percentage + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // 当没有选择任何值时执行的操作，可根据需要留空
            }
        });


        return root;
    }

//    数据查询与更新
    private void update(int year, int month, int dayOfMonth) {
//        启用后台线程
        new Thread(() -> {
//            获取指定日期的收入、支出和支出分类数据
            i = DatabaseAction.getInstance(getContext()).getAllIncomesDao().dayIn(year, month, dayOfMonth);
            o = DatabaseAction.getInstance(getContext()).getAllIncomesDao().dayOut(year, month, dayOfMonth);
            db = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getDayExpense(year, month, dayOfMonth);
            values = new ArrayList<>();

            HashMap<String, Long> categoryAmountMap = new HashMap<>();

            for (MyDatabase d : db) {
//                SliceValue sliceValue = new SliceValue((float) ((double) d.getMoney() / 100), ChartUtils.pickColor());
//                values.add(sliceValue);
                String category = d.getType();
                long amount = d.getMoney();
                if (categoryAmountMap.containsKey(category)) {
                    amount += categoryAmountMap.get(category);
                }
                categoryAmountMap.put(category, amount);
            }

            for (Map.Entry<String, Long> entry : categoryAmountMap.entrySet()) {
                String category = entry.getKey();
                long amount = entry.getValue();
                SliceValue sliceValue = new SliceValue((float) (amount / 100.0), ChartUtils.pickColor());
                sliceValue.setLabel(category);
                values.add(sliceValue);
            }

//            将数据封装成PieChartData对象
            data = new PieChartData(values);
            data.setHasLabels(true);

//            通过handler.sendMessage(msg)发送消息
            Message msg = new Message();
            msg.what = COMPLETED;    //通知主线程数据加载完成
            handler.sendMessage(msg);
        }).start();
    }

    public final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
//                更新UI元素的内容，包括设置收入、支出和净收入的文本以及设置饼状图的数据
                im.setText(String.format("%.2f", (double) i / 100));
                om.setText(String.format("%.2f", (double) o / 100));
                am.setText(String.format("%.2f", (double) i / 100 - (double) o / 100));
                pieChart.setPieChartData(data);
            }
        }
    };

    // 点击饼图模块时显示对应的类别、金额和百分比
    private void showSliceDetails(int sliceIndex) {
        if (db != null && sliceIndex >= 0 && sliceIndex < db.size()) {
            MyDatabase selectedDatabase = db.get(sliceIndex);
            String category = selectedDatabase.getType();
            double amount = selectedDatabase.getMoney() / 100.0;
            double percentage = (selectedDatabase.getMoney() / (double) (i + o)) * 100.0;

            String sliceDetails = String.format("类别: %s\n金额: %.2f\n百分比: %.2f%%", category, amount, percentage);
            // 在这里显示类别-金额-百分比的信息，例如可以使用 Toast 或者设置一个 TextView 进行显示
            // 例如：Toast.makeText(getContext(), sliceDetails, Toast.LENGTH_SHORT).show();
        }
    }

    // 计算饼图的总值
    private float getTotalValue() {
        float totalValue = 0;
        for (SliceValue value : values) {
            totalValue += value.getValue();
        }
        return totalValue;
    }


}
