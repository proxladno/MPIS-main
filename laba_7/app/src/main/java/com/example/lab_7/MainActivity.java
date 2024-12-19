package com.example.lab_7;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lab_7.Adapters.ViewPagerAdapter;
import com.example.lab_7.Fragments.AudioFragment;
import com.example.lab_7.Fragments.PhotoFragment;
import com.example.lab_7.Fragments.VideoFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private void setMargin(View view, boolean left, boolean top, boolean right, boolean bottom) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            layoutParams.leftMargin = left ? insets.left : 0;
            layoutParams.topMargin = top ? insets.top : 0;
            layoutParams.rightMargin = right ? insets.right : 0;
            layoutParams.bottomMargin = bottom ? insets.bottom : 0;
            v.setLayoutParams(layoutParams);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.ViewPager);

        setMargin(tabLayout, false, true, false, false);
        setMargin(viewPager, false, false, false, true);
        setMargin(findViewById(R.id.textViewFIO), false, false, false, true);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new AudioFragment());
        fragmentList.add(new VideoFragment());
        fragmentList.add(new PhotoFragment());

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String[] titles = {"Аудио", "Видео", "Фото"};
            tab.setText(titles[position]);
        }).attach();
    }

}