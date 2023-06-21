package com.leikz.moneytracker.ui.chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.leikz.moneytracker.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.leikz.moneytracker.databinding.FragmentChartBinding;

public class ChartFragment extends Fragment {

    private FragmentChartBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        实现滑动切换不同视图
        ViewPager2 viewPager2 = root.findViewById(R.id.viewpager2);
        viewPager2.setAdapter(new ViewPagerAdapter2(this));
        TabLayout tabLayout = root.findViewById(R.id.tabs2);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("年视图");
                    break;
                case 1:
                    tab.setText("月视图");
                    break;
                default:
                    tab.setText("日视图");
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
}

class ViewPagerAdapter2 extends FragmentStateAdapter {

    public ViewPagerAdapter2(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return YearView.newInstance("fragment1", "f1");
            case 1:
                return MonthView.newInstance("fragment2", "f2");
            default:
                return DayView.newInstance("fragment3", "f3");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}