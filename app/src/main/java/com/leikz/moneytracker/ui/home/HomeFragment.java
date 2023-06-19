package com.leikz.moneytracker.ui.home;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.leikz.moneytracker.R;
import com.leikz.moneytracker.database.DatabaseAction;
import com.leikz.moneytracker.database.MyDatabase;
import com.leikz.moneytracker.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.leikz.moneytracker.OCR.getOCR;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public static String type = "餐饮";
    public static boolean inOut = true;
    public static String remark = "";
    public static long moneyAcc = 0;
    public static int[] date = {0, 0, 0};

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.floatingAddButton.setOnClickListener(view -> show());
        ViewPager2 viewPager2 = root.findViewById(R.id.viewpager);
        viewPager2.setAdapter(new ViewPagerAdapter(this));
        TabLayout tabLayout = root.findViewById(R.id.tabs);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("全部");
                    tab.setIcon(R.drawable.ic_all_items);
                    break;
                case 1:
                    tab.setText("支出");
                    tab.setIcon(R.drawable.ic_expenditure);
                    break;
                default:
                    tab.setText("收入");
                    tab.setIcon(R.drawable.ic_income);
                    break;
            }

        });
        mediator.attach();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void show() {
        type = "餐饮";
        inOut = true;
        Dialog dialog = new Dialog(this.getContext());
        //去掉标题线
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.add_dialog, null, false);
        dialog.setContentView(view);
        //背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.animStyle);  //添加动画

//        设置收入和支出的选项卡页面
        ViewPager2 viewPager3 = view.findViewById(R.id.typePage);
        viewPager3.setAdapter(new TypePageAdapter(this));
        TabLayout tabLayout1 = view.findViewById(R.id.typeTab);
//        根据选项卡的位置返回相应的实例
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout1, viewPager3, (tab, position) -> {
            if (position == 0)
                tab.setText("支出");
            else
                tab.setText("收入");
        });
        mediator.attach();

//        创建资源文件实例
        TextView chooseDate = view.findViewById(R.id.chooseDate);
        EditText moneyTxt = view.findViewById(R.id.money);
        EditText remarkTxt = view.findViewById(R.id.remarkText);

        Button ocrButton = view.findViewById(R.id.ocrButton);
        Button addBt = view.findViewById(R.id.addBt);

//        选择日期
        chooseDate.setOnClickListener(v -> {
//            获取实例，包含当前年月日
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog1 = new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) -> {
                chooseDate.setText(String.format(getString(R.string.chooseDate), year, month + 1, dayOfMonth));
//                Toast.makeText(getContext(), year + "-" + (month + 1) + "-" + dayOfMonth, Toast.LENGTH_SHORT).show();
                date[0] = year;
                date[1] = month + 1;
                date[2] = dayOfMonth;
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog1.show();
        });

//        填写金额
        moneyTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //如果第一个数字为0，第二个不为点，就不允许输入
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (s.toString().charAt(1) != '.') {
                        moneyTxt.setText(s.subSequence(0, 1));
                        moneyTxt.setSelection(1);
                        return;
                    }
                }
                //如果第一为点，直接显示0.
                if (s.toString().startsWith(".")) {
                    moneyTxt.setText("0.");
                    moneyTxt.setSelection(2);
                    return;
                }
                //限制输入小数位数(2位)
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 2 + 1);
                        moneyTxt.setText(s);
                        moneyTxt.setSelection(s.length());
                    }
                    if (s.toString().indexOf(".") > 8) {
                        s = s.toString().substring(0, s.toString().indexOf(".") - 1) + s.toString().substring(s.toString().indexOf("."), s.length());
                        moneyTxt.setText(s);
                        moneyTxt.setSelection(s.length());
                    }
                }
                //限制整数部分长度
                if (!s.toString().contains(".")) {
                    if (s.length() > 8) {
                        moneyTxt.setText(s.subSequence(0, s.length() - 1));
                        moneyTxt.setSelection(s.length() - 1);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        添加
        addBt.setOnClickListener(v -> {
            if (String.valueOf(chooseDate.getText()).equals("今天")) {
                date[0] = Calendar.getInstance().get(Calendar.YEAR);
                date[1] = Calendar.getInstance().get(Calendar.MONTH) + 1;
                date[2] = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            }
            remark = String.valueOf(remarkTxt.getText());
            double mn = String.valueOf(moneyTxt.getText()).equals("") ? 0 : Double.parseDouble(String.valueOf(moneyTxt.getText()));
            moneyAcc = (long) (mn * 100);
            Log.d("TAG", "onClick: " + mn + "  " + moneyAcc);
            new Thread(() -> {
                DatabaseAction.getInstance(getContext()).getAllIncomesDao().insert(new MyDatabase(moneyAcc, date[0], date[1], date[2], type, remark, inOut));
                AllItems.allItemList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllAccounts();
                Message msg = new Message();
                msg.what = AllItems.COMPLETED;
                AllItems.handler.sendMessage(msg);
                Message msg2 = new Message();
                msg2.what = AllItems.COMPLETED;
                if (inOut) {
                    Expenditure.expenditureList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllExpense();
                    Expenditure.handler.sendMessage(msg2);
                } else {
                    Income.incomeList = DatabaseAction.getInstance(getContext()).getAllIncomesDao().getAllIncomes();
                    Income.handler.sendMessage(msg2);
                }
                Looper.prepare();
                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }).start();
            dialog.dismiss();
        });

//        OCR识别填充
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                打开系统图库
                openGallery();

                // 执行OCR识别
//                performOcrAndFillFields();
//                getOCR.shoppingReceipt();
            }
        });
    }


//    定义用于打开系统图库的函数
    private static final int REQUEST_GALLERY = 1;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                // 处理图库返回的图片URI，例如执行OCR识别等操作
                performOcrAndFillFields(imageUri);
            }
        }
    }

    private void performOcrAndFillFields(Uri imageUri) {
        String imagePath = getImagePathFromUri(imageUri); // 将URI转换为路径
        getOCR.shoppingReceipt(imagePath); // 调用OCR识别方法
    }

    private String getImagePathFromUri(Uri imageUri) {
        String imagePath = null;
        if (imageUri != null) {
//            Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
            Cursor cursor = requireContext().getContentResolver().query(imageUri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                imagePath = cursor.getString(index);
                cursor.close();
            }
        }
        return imagePath;
    }
}

class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return AllItems.newInstance("fragment1", "f1");
            case 1:
                return Expenditure.newInstance("fragment2", "f2");
            default:
                return Income.newInstance("fragment3", "f3");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

class TypePageAdapter extends FragmentStateAdapter {

    public TypePageAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//        如果选项卡位置为 0，则返回 AddExpenditure 实例；如果选项卡位置为 1，则返回 AddIncome 实例。
        return (position == 0) ? AddExpenditure.newInstance("fragment4", "f4") : AddIncome.newInstance("fragment5", "f5");
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}