package com.leikz.moneytracker.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leikz.moneytracker.AccountItemAdapter;
import com.leikz.moneytracker.R;
import com.leikz.moneytracker.database.DatabaseAction;
import com.leikz.moneytracker.database.MyDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//显示支出列表
public class Expenditure extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
//    静态的支出列表
    public static List<MyDatabase> expenditureList = new ArrayList<>();
//    显示列表数据的适配器
    public static AccountItemAdapter adapter = new AccountItemAdapter(expenditureList);

    public Expenditure() {
        // Required empty public constructor
    }

    /**
     * Create a new instance of the Expenditure fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of the Expenditure fragment.
     */
    // TODO: Rename and change types and number of parameters if needed
    public static Expenditure newInstance(String param1, String param2) {
        Expenditure fragment = new Expenditure();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.expenditure, container, false);
//        支出列表
        RecyclerView recyclerView = root.findViewById(R.id.expenditureList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refreshList();
        adapter.setOnItemClickListener(new AccountItemAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View v) {
                Dialog dialog = new Dialog(getContext());
                //去掉标题线
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.detail, null, false);
                MyDatabase mdb = expenditureList.get(position);
                TextView txt = view.findViewById(R.id.detailInOut);
                txt.setText((mdb.getInOut() ? "支出" : "收入"));
                txt = view.findViewById(R.id.detailType);
                txt.setText("：" + mdb.getType());
                txt = view.findViewById(R.id.detailDate);
                txt.setText(String.format(Locale.CHINA, "%d年%d月%d日", mdb.getYear(), mdb.getMonth(), mdb.getDay()));
                txt = view.findViewById(R.id.detailMoney);
                txt.setText(String.format("%.2f", (double) mdb.getMoney() / 100));
                txt = view.findViewById(R.id.detailRemark);
                txt.setText(mdb.getRemark());
                dialog.setContentView(view);
                //背景透明
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }

            @Override
            public void onLongClick(int position, View v) {
                editMenu(position, v);
            }
        });
        return root;
    }

    private void editMenu(int position, View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.long_click_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete) {
                new Thread(() -> {
                    DatabaseAction.getInstance(getContext()).getAllIncomesDao().delete(expenditureList.get(position));
                    AllItems.allItemList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllAccounts();
                    Message msg = new Message();
                    msg.what = AllItems.COMPLETED;
                    AllItems.handler.sendMessage(msg);
                    Message msg2 = new Message();
                    msg2.what = AllItems.COMPLETED;
                    expenditureList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllExpense();
                    handler.sendMessage(msg2);
                    Looper.prepare();
                    Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }).start();
            }
            return false;
        });
        popupMenu.show();
    }

    public static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == AllItems.COMPLETED) {
                adapter.setData(expenditureList);
                adapter.notifyDataSetChanged();
            }
        }
    };


//    在后台线程中刷新支出列表数据，并通知适配器进行更新。
    private void refreshList() {
        new Thread(() -> {
            expenditureList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllExpense();
            Message msg = new Message();
            msg.what = AllItems.COMPLETED;
            handler.sendMessage(msg);
        }).start();
    }
}
