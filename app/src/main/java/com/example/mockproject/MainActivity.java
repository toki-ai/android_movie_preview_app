package com.example.mockproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.fragment.AboutFragment;
import com.example.mockproject.fragment.FavoriteFragment;
import com.example.mockproject.fragment.ListMoviesFragment;
import com.example.mockproject.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment currentFragment;
    private OnToolbarClickListener toolbarClickListener;
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;
    private boolean isGrid = false;
    private ImageButton toolbarIconList;
    private ImageView toolbarOptionsMenu;
    private SharedPreferences sharedPreferences;
    public static final String USER_ID = "user_id";
    public static final String SHARE_KEY = "mock_prj";

    public void setToolbarClickListener(OnToolbarClickListener listener) {
        this.toolbarClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);

        setUpToolbar();
        setUpOptionsMenu();
        setUpDrawer();
        setUpBottomNavAndVisibleToolbar();
        setUpToolbarIconList();
    }

    private void setUpBottomNavAndVisibleToolbar(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarIconList = findViewById(R.id.toolbar_icon_list);

        currentFragment = new ListMoviesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, currentFragment, "Movies")
                .commit();

        setToolbarClickListener((OnToolbarClickListener) currentFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "Movies";
            boolean isVisibleIconList = true;
            boolean isVisibleOpsMenu = true;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = getOrCreateFragment(ListMoviesFragment.class, "Movies");
                title = "Movies";
                toolbarIconList.setImageResource(R.drawable.icon_toolbar_grid);
            } else if (item.getItemId() == R.id.nav_favorite) {
                selectedFragment = getOrCreateFragment(FavoriteFragment.class, "Favorites");
                title = "Favorites";
                isVisibleOpsMenu = false;
                toolbarIconList.setImageResource(R.drawable.icon_toolbar_search);
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = getOrCreateFragment(SettingsFragment.class, "Settings");
                title = "Settings";
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            } else if (item.getItemId() == R.id.nav_about) {
                selectedFragment = getOrCreateFragment(AboutFragment.class, "About");
                title = "About";
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            }

            if (selectedFragment != null) {
                switchFragment(selectedFragment);
                toolbarTitle.setText(title);
                toolbarIconList.setVisibility(isVisibleIconList ? View.VISIBLE : View.GONE);
                toolbarOptionsMenu.setVisibility(isVisibleOpsMenu ? View.VISIBLE : View.GONE);
            }
            return true;
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

    private void setUpToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setUpToolbarIconList(){
        toolbarIconList.setOnClickListener(v -> {
            if (toolbarClickListener != null) {
                isGrid = !isGrid;
                toolbarClickListener.onToolbarIconClick();
                toolbarIconList.setImageResource(isGrid ? R.drawable.icon_toolbar_list : R.drawable.icon_toolbar_grid);
            }
        });
    }

    private void setUpOptionsMenu(){
        toolbarOptionsMenu = findViewById(R.id.toolbar_icon_more);
        toolbarOptionsMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.op_menu_nowPlaying) {
                    toolbarClickListener.onToolbarOpsClick(ListMoviesFragment.TYPE_NOW_PLAYING);
                    return true;
                }else if(item.getItemId() == R.id.op_menu_popular) {
                    toolbarClickListener.onToolbarOpsClick(ListMoviesFragment.TYPE_POPULAR);
                    return true;
                }else if(item.getItemId() == R.id.op_menu_upcoming) {
                    toolbarClickListener.onToolbarOpsClick(ListMoviesFragment.TYPE_UPCOMING);
                    return true;
                }else if(item.getItemId() == R.id.op_menu_topRated) {
                    toolbarClickListener.onToolbarOpsClick(ListMoviesFragment.TYPE_TOP_RATED);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void setUpDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton btnToggle = findViewById(R.id.toolbar_icon_burger);
        NavigationView navigationView = findViewById(R.id.drawer_nav);

        navigationView.removeHeaderView(navigationView.getHeaderView(0));
        String userId = sharedPreferences.getString(USER_ID, "");
        int headerLayout = userId.isEmpty() ? R.layout.header_drawer_profile : R.layout.header_drawer_guest;
        View headerView = getLayoutInflater().inflate(headerLayout, navigationView, false);
        navigationView.addHeaderView(headerView);

        if (!userId.isEmpty()){
            login();
        }else{
            loadDrawerData();
        }

        btnToggle.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });
    }

    private void login(){

    }

    private void loadDrawerData(){
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                if (bitmap != null) {
//                    ivAvatar.setImageBitmap(bitmap);
//                    saveImageToPreferences(bitmap);
                }
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getData().getData());
//                    ivAvatar.setImageBitmap(bitmap);
//                    saveImageToPreferences(bitmap);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    private void setProfileEditMode(boolean isEdit){

    }

}
