package com.leikz.moneytracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leikz.moneytracker.database.MyDatabase;

import java.util.List;
import java.util.Locale;

public class AccountItemAdapter extends RecyclerView.Adapter<AccountItemAdapter.ViewHolder> {
    private List<MyDatabase> itemsList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemType;
        TextView itemDate;
        TextView itemMoney;

        public ViewHolder(View view) {
            super(view);
            itemIcon = view.findViewById(R.id.itemIcon);
            itemType = view.findViewById(R.id.itemType);
            itemDate = view.findViewById(R.id.itemDate);
            itemMoney = view.findViewById(R.id.moneyAcc);
        }
    }

    public AccountItemAdapter(List<MyDatabase> itemsList) {
        this.itemsList = itemsList;
    }

//    更新数据列表
    public void setData(List<MyDatabase> itemsList) {
        this.itemsList = itemsList;
    }

//    项目点击事件监听器
    public interface OnItemClickListener {
        void onClick(int position, View v);

        void onLongClick(int position, View v);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        创建 ViewHolder 实例，并关联 item 的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        绑定数据到 ViewHolder 中的视图项
        MyDatabase cd = itemsList.get(position);
        holder.itemMoney.setText((cd.getInOut() ? "-" : "+") + String.format("%.2f", (double) cd.getMoney() / 100));
        holder.itemDate.setText(String.format(Locale.CHINA, "%d年%d月%d日", cd.getYear(), cd.getMonth(), cd.getDay()));
        holder.itemType.setText(cd.getRemark().equals("") ? cd.getType() : cd.getRemark());

        // 根据类型设置图标资源
        switch (cd.getType()) {
            case "餐饮":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_catering);
                break;
            case "日用":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_daily);
                break;
            case "服饰":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_clothes);
                break;
            case "购物":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_shopping);
                break;
            case "交通":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_traffic);
                break;
            case "医药":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_medicine);
                break;
            case "办公":
                holder.itemIcon.setImageResource(R.drawable.ic_expenditure_work);
                break;
            case "工资":
                holder.itemIcon.setImageResource(R.drawable.ic_income_salary);
                break;
            case "理财":
                holder.itemIcon.setImageResource(R.drawable.ic_income_wealth_management);
                break;
            case "礼金":
                holder.itemIcon.setImageResource(R.drawable.ic_income_gift);
                break;
            default:
                holder.itemIcon.setImageResource(cd.getInOut() ? R.drawable.ic_expenditure_other : R.drawable.ic_income_other);
                break;
        }

        // 设置项目项的点击事件监听器
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(holder.getAdapterPosition(), v));
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemClickListener.onLongClick(holder.getAdapterPosition(), v);
                return true;
            });
        }
    }

    // 返回项目项的数量
    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}
