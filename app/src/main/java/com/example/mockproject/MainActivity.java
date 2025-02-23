package com.example.mockproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mockproject.fragment.AboutFragment;
import com.example.mockproject.fragment.FavoriteFragment;
import com.example.mockproject.fragment.ListMoviesFragment;
import com.example.mockproject.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment currentFragment;
    private OnToolbarIconClickListener toolbarIconClickListener;
    private boolean isGrid = false;

    public void setToolbarIconClickListener(OnToolbarIconClickListener listener) {
        this.toolbarIconClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        setUpDrawer();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton toolbarIconList = findViewById(R.id.toolbar_icon_list);

        toolbarIconList.setOnClickListener(v -> {
            if (toolbarIconClickListener != null) {
                isGrid = !isGrid;
                toolbarIconClickListener.onToolbarIconClick();
                toolbarIconList.setImageResource(isGrid ? R.drawable.icon_list : R.drawable.icon_grid);
            }
        });

        currentFragment = new ListMoviesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, currentFragment, "Movies")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "Movies";
            boolean isVisibleIconList = true;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = getOrCreateFragment(ListMoviesFragment.class, "Movies");
                title = "Movies";
                toolbarIconList.setImageResource(R.drawable.icon_grid);
            } else if (item.getItemId() == R.id.nav_favorite) {
                selectedFragment = getOrCreateFragment(FavoriteFragment.class, "Favorites");
                title = "Favorites";
                toolbarIconList.setImageResource(R.drawable.icon_search);
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = getOrCreateFragment(SettingsFragment.class, "Settings");
                title = "Settings";
                isVisibleIconList = false;
            } else if (item.getItemId() == R.id.nav_about) {
                selectedFragment = getOrCreateFragment(AboutFragment.class, "About");
                title = "About";
                isVisibleIconList = false;
            }

            if (selectedFragment != null) {
                switchFragment(selectedFragment);
                toolbarTitle.setText(title);
                toolbarIconList.setVisibility(isVisibleIconList ? View.VISIBLE : View.GONE);
            }
            return true;
        });
    }

    private void setUpToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setUpDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton btnToggle = findViewById(R.id.toolbar_icon_burger);

        btnToggle.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private Fragment getOrCreateFragment(Class<? extends Fragment> fragmentClass, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            try {
                fragment = fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fragment;
    }

    private void switchFragment(Fragment newFragment) {
        if (currentFragment == newFragment) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!newFragment.isAdded()) {
            transaction.add(R.id.frame_container, newFragment, newFragment.getClass().getSimpleName());
        }
        transaction.hide(currentFragment).show(newFragment).commit();

        currentFragment = newFragment;
    }
}
