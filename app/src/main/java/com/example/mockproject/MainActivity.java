package com.example.mockproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.callback.OnLoginRequestListener;
import com.example.mockproject.callback.OnUpdateFavoriteListListener;
import com.example.mockproject.callback.OnUpdateMovieListListener;
import com.example.mockproject.callback.OnUpdateStarFavoriteListener;
import com.example.mockproject.database.MovieRepository;
import com.example.mockproject.database.ReminderRepository;
import com.example.mockproject.database.UserRepository;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.entities.User;
import com.example.mockproject.fragment.AboutFragment;
import com.example.mockproject.fragment.FavoriteFragment;
import com.example.mockproject.fragment.ListMoviesFragment;
import com.example.mockproject.fragment.MovieDetailFragment;
import com.example.mockproject.fragment.SettingsFragment;
import com.example.mockproject.fragment.adapter.MovieAdapter;
import com.example.mockproject.fragment.adapter.ReminderAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnLoginRequestListener, OnUpdateStarFavoriteListener {

    private Fragment currentFragment;
    private OnUpdateMovieListListener onUpdateMovieListListener;
    private OnUpdateFavoriteListListener onUpdateFavListListener;
    private UserRepository userRepository;
    private MovieRepository movieRepository;
    private SharedPreferences sharedPreferences;
    private FrameLayout detailFrameContainer, frameContainer;
    private ImageButton toolbarIconList, toolbarOptionsMenu, toolbarSearch, toolbarBack, toolbarBurger;
    private ImageView profileAvatar;
    private TextView profileUsername, profileEmail, profileBirthday, toolbarTitle, btnEdit, btnLogout, btnCancel, btnSave;
    private EditText toolbarSearchInput;
    private RadioButton isMale, isFemale;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    public static final String USER_ID = "user_id";
    public static final String SHARE_KEY = "mock_prj";
    private boolean isSearching = false;
    private boolean isGrid = false;
    private enum FRAGMENT_TAG { MOVIE, FAVORITE, SETTING, ABOUT }

    public void setOnUpdateMovieListListener(OnUpdateMovieListListener listener) {
        this.onUpdateMovieListListener = listener;
    }

    public void setOnUpdateFavListListener(OnUpdateFavoriteListListener listener) {
        this.onUpdateFavListListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userRepository = new UserRepository(MainActivity.this);
        movieRepository = new MovieRepository(MainActivity.this);

        sharedPreferences = getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);

        detailFrameContainer = findViewById(R.id.detail_frame_container);
        frameContainer = findViewById(R.id.frame_container);

        setUpToolbar();
        setUpEagerFavoriteFragment();
        setUpDrawer();
        setUpBottomNavAndVisibleToolbar();
    }

    private void setUpEagerFavoriteFragment() {
        FavoriteFragment favoriteFragment = (FavoriteFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(FRAGMENT_TAG.FAVORITE));
        if (favoriteFragment == null) {
            favoriteFragment = new FavoriteFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_container, favoriteFragment, String.valueOf(FRAGMENT_TAG.FAVORITE))
                    .hide(favoriteFragment)
                    .commit();
        }
        setOnUpdateFavListListener(favoriteFragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUpBottomNavAndVisibleToolbar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        currentFragment = new ListMoviesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, currentFragment, String.valueOf(FRAGMENT_TAG.MOVIE))
                .commit();
        setOnUpdateMovieListListener((OnUpdateMovieListListener) currentFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            setDetailDisplay(false);
            Fragment selectedFragment = null;
            String title = String.valueOf(FRAGMENT_TAG.MOVIE);
            boolean isVisibleIconList = true;
            boolean isVisibleOpsMenu = true;
            boolean isVisibleSearch = false;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = getOrCreateFragment(ListMoviesFragment.class, String.valueOf(FRAGMENT_TAG.MOVIE));
                title = "Movies";
            } else if (itemId == R.id.nav_favorite) {
                selectedFragment = getOrCreateFragment(FavoriteFragment.class, String.valueOf(FRAGMENT_TAG.FAVORITE));
                title = "Favorites";
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
                isVisibleSearch = true;
            } else if (itemId == R.id.nav_setting) {
                selectedFragment = getOrCreateFragment(SettingsFragment.class, String.valueOf(FRAGMENT_TAG.SETTING));
                title = "Settings";
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            } else if (itemId == R.id.nav_about) {
                selectedFragment = getOrCreateFragment(AboutFragment.class, String.valueOf(FRAGMENT_TAG.ABOUT));
                title = "About";
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            }

            if (selectedFragment != null) {
                switchFragment(selectedFragment);
                toolbarTitle.setText(title);
                toolbarSearchInput.setVisibility(isVisibleSearch ? View.VISIBLE : View.GONE);
                toolbarTitle.setVisibility(!isVisibleSearch ? View.VISIBLE : View.GONE);
                toolbarSearch.setVisibility(isVisibleSearch ? View.VISIBLE : View.GONE);
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
                Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
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

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarSearchInput = findViewById(R.id.toolbar_search_input);
        toolbarSearch = findViewById(R.id.toolbar_icon_search);
        toolbarIconList = findViewById(R.id.toolbar_icon_list);
        toolbarOptionsMenu = findViewById(R.id.toolbar_icon_more);
        toolbarBack = findViewById(R.id.toolbar_icon_back);
        toolbarBurger = findViewById(R.id.toolbar_icon_burger);

        setUpToolbarOptionsMenu();
        setUpToolbarIconList();

        toolbarSearch.setOnClickListener(view -> {
            if (!isSearching) {
                isSearching = true;
                toolbarTitle.setVisibility(View.GONE);
                toolbarSearchInput.setText("");
                toolbarSearchInput.setVisibility(View.VISIBLE);
                toolbarSearchInput.requestFocus();
            } else {
                String keyword = toolbarSearchInput.getText().toString().trim();
                doSearchFavorites(keyword);
            }
        });
    }
    private void doSearchFavorites(String keyword) {
        String userIdStr = sharedPreferences.getString(USER_ID, "");
        if (userIdStr.isEmpty()) {
            return;
        }
        int userId = Integer.parseInt(userIdStr);

        MovieRepository repo = new MovieRepository(this);
        List<Movie> searchResults = repo.getFavMoviesByKeyword(userId, keyword);

        Fragment current = getSupportFragmentManager().findFragmentByTag("Favorites");
        if (current instanceof FavoriteFragment) {
            ((FavoriteFragment) current).showSearchResults(searchResults);
        }
    }

    private void setUpToolbarIconList() {
        toolbarIconList.setOnClickListener(v -> {
            if (onUpdateMovieListListener != null) {
                isGrid = !isGrid;
                onUpdateMovieListListener.onToolbarIconClick();
                toolbarIconList.setImageResource(isGrid
                        ? R.drawable.icon_toolbar_list
                        : R.drawable.icon_toolbar_grid);
            }
        });
    }

    private void setUpToolbarOptionsMenu() {
        toolbarOptionsMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (onUpdateMovieListListener == null) return false;
                int itemId = item.getItemId();

                if (itemId == R.id.op_menu_nowPlaying) {
                    onUpdateMovieListListener.onToolbarOpsClick(ListMoviesFragment.TYPE_NOW_PLAYING);
                    return true;
                } else if (itemId == R.id.op_menu_popular) {
                    onUpdateMovieListListener.onToolbarOpsClick(ListMoviesFragment.TYPE_POPULAR);
                    return true;
                } else if (itemId == R.id.op_menu_upcoming) {
                    onUpdateMovieListListener.onToolbarOpsClick(ListMoviesFragment.TYPE_UPCOMING);
                    return true;
                } else if (itemId == R.id.op_menu_topRated) {
                    onUpdateMovieListListener.onToolbarOpsClick(ListMoviesFragment.TYPE_TOP_RATED);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void setUpDrawer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.drawer_nav);
        navigationView.removeHeaderView(navigationView.getHeaderView(0));
        String userId = sharedPreferences.getString(USER_ID, "");
        int headerLayout = !userId.isEmpty()
                ? R.layout.header_drawer_profile
                : R.layout.header_drawer_guest;
        View headerView = getLayoutInflater().inflate(headerLayout, navigationView, false);
        navigationView.addHeaderView(headerView);
        if (userId.isEmpty()) {
            login(headerView, drawerLayout, navigationView);
        } else {
            loadDrawerData(headerView, userId);
        }
        toolbarBurger.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });
    }

    private void login(View headerView, DrawerLayout drawerLayout, NavigationView navigationView) {
        TextView btnLogin = headerView.findViewById(R.id.profile_btn_login);
        btnLogin.setOnClickListener(v -> {
            drawerLayout.closeDrawer(navigationView);
            showBottomDialog(MainActivity.this);
        });
    }

    public void showBottomDialog(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.dialog_login);
        EditText inputEmail = bottomSheetDialog.findViewById(R.id.login_email);
        EditText inputUsername = bottomSheetDialog.findViewById(R.id.login_username);
        TextView btnSignIn = bottomSheetDialog.findViewById(R.id.login_submit);
        if(btnSignIn == null){
            return;
        }
        btnSignIn.setOnClickListener(v -> {
            String email = (inputEmail != null ? inputEmail.getText().toString().trim() : "");
            String username = (inputUsername != null ? inputUsername.getText().toString().trim() : "");
            if (email.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "Please input email and username", Toast.LENGTH_SHORT).show();
                return;
            }
            long userId = userRepository.login(username, null, email, null, null);
            if (userId > 0) {
                sharedPreferences.edit().putString(USER_ID, String.valueOf(userId)).apply();
                Toast.makeText(context, "Login successfully!", Toast.LENGTH_SHORT).show();
                setUpDrawer();
                bottomSheetDialog.dismiss();
                if (onUpdateFavListListener != null) {
                    onUpdateFavListListener.onUpdateFavoriteList();
                }
            }
        });
        bottomSheetDialog.show();
    }

    private void loadDrawerData(View headerView, String userId) {
        profileAvatar = headerView.findViewById(R.id.profile_avatar);
        profileUsername = headerView.findViewById(R.id.profile_name);
        profileEmail = headerView.findViewById(R.id.profile_mail);
        profileBirthday = headerView.findViewById(R.id.profile_birthday);
        isMale = headerView.findViewById(R.id.radio_male);
        isFemale = headerView.findViewById(R.id.radio_female);
        btnLogout = headerView.findViewById(R.id.profile_btn_logout);
        btnEdit = headerView.findViewById(R.id.profile_btn_edit);
        btnCancel = headerView.findViewById(R.id.profile_btn_cancel);
        btnSave = headerView.findViewById(R.id.profile_btn_save);
        User account = userRepository.getUserById(Integer.parseInt(userId));
        profileUsername.setText(account.getName());
        profileEmail.setText(account.getEmail());
        if (account.isGender()) {
            isMale.setChecked(true);
        } else {
            isFemale.setChecked(true);
        }
        if (account.getBirthday() != null) {
            profileBirthday.setText(account.getBirthday());
        }
        if (account.getImage() != null) {
            try {
                byte[] decodedBytes = Base64.decode(account.getImage(), Base64.DEFAULT);
                Bitmap avatarBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileAvatar.setImageBitmap(avatarBitmap);
            } catch (Exception e) {
                Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
            }
        }
        TextView btnShowReminder = headerView.findViewById(R.id.reminder_btn_show);
        btnShowReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        RecyclerView reminderRecycler = headerView.findViewById(R.id.reminder_short_list);
        reminderRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        ReminderRepository reminderRepository = new ReminderRepository(this);
        List<Reminder> allReminders = reminderRepository.getRemindersByUser(Integer.parseInt(userId));
        List<Reminder> shortList;
        if (allReminders.size() > 3) {
            shortList = allReminders.subList(0, 3);
        } else {
            shortList = allReminders;
        }
        ReminderAdapter reminderAdapter = new ReminderAdapter(this, shortList, null);
        reminderRecycler.setAdapter(reminderAdapter);

        btnLogout.setOnClickListener(v -> {
            sharedPreferences.edit().remove(USER_ID).apply();
            Toast.makeText(MainActivity.this, "Logout successfully!", Toast.LENGTH_SHORT).show();
            if (onUpdateFavListListener != null) {
                onUpdateFavListListener.onUpdateFavoriteUILogin();
            }
            setUpDrawer();
        });
        btnEdit.setOnClickListener(v -> setProfileEditMode(true));
        btnCancel.setOnClickListener(v -> {
            setProfileEditMode(false);
            loadDrawerData(headerView, userId);
        });
        btnSave.setOnClickListener(v -> {
            saveProfileToDatabase(userId);
            setProfileEditMode(false);
            loadDrawerData(headerView, userId);
        });
    }

    private void setProfileEditMode(boolean isEdit) {
        isMale.setEnabled(isEdit);
        isFemale.setEnabled(isEdit);
        profileUsername.setEnabled(isEdit);
        profileEmail.setEnabled(isEdit);
        profileBirthday.setEnabled(isEdit);
        btnLogout.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        btnEdit.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        if (isEdit) {
            profileAvatar.setOnClickListener(v -> showMediaPopup());
            profileBirthday.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        (view, year, month, dayOfMonth) -> {
                            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                            profileBirthday.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            });
        } else {
            profileAvatar.setOnClickListener(null);
            profileBirthday.setOnClickListener(null);
        }
    }

    private void showMediaPopup() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, profileAvatar);
        popupMenu.getMenuInflater().inflate(R.menu.camera_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.camera_menu_camera) {
                cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            } else if (itemId == R.id.camera_menu_gallery) {
                galleryLauncher.launch(new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ));
            }
            return true;
        });
        popupMenu.show();
    }

    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveProfileToDatabase(String userId) {
        String name = profileUsername.getText().toString().trim();
        String email = profileEmail.getText().toString().trim();
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill username and email", Toast.LENGTH_SHORT).show();
            return;
        }
        String birthday = profileBirthday.getText().toString().trim();
        int gender = isMale.isChecked() ? 1 : 0;
        String image = null;
        Drawable drawable = profileAvatar.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                image = bitmapToString(bitmap);
            }
        }
        int rowsUpdated = userRepository.updateUserProfile(
                Integer.parseInt(userId), name, birthday, email, image, gender
        );
        if (rowsUpdated > 0) {
            Toast.makeText(MainActivity.this, "Update profile successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Fail to update Profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoginRequested() {
        showBottomDialog(MainActivity.this);
    }

    public void setDetailDisplay(boolean isDisplay){
        detailFrameContainer.setVisibility(isDisplay ? View.VISIBLE : View.GONE);
        toolbarBack.setVisibility(isDisplay ? View.VISIBLE : View.GONE);

        frameContainer.setVisibility(isDisplay ? View.GONE : View.VISIBLE);
        toolbarBurger.setVisibility(isDisplay ? View.GONE : View.VISIBLE);
    }

    public void openDetailFragment(int movieId, String movieTitle) {
        setDetailDisplay(true);

        MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movieId, movieTitle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_frame_container, detailFragment, "MovieDetail")
                .addToBackStack("Detail")
                .commit();
        toolbarTitle.setText(movieTitle);

        toolbarBack.setOnClickListener(v -> {
            setDetailDisplay(false);
            if (currentFragment instanceof ListMoviesFragment){
                toolbarTitle.setText("Movies");
            }else if (currentFragment instanceof FavoriteFragment){
                toolbarTitle.setText("Favorites");
            }
        });
    }



    @Override
    public void onUpdateStartFavorite(Movie movie, MovieAdapter.TYPE type) {
        movieRepository.handleClickFavMovie(movie);
        if (type.equals(MovieAdapter.TYPE.LIST)) {
            if (onUpdateFavListListener != null) {
                onUpdateFavListListener.onUpdateFavoriteList();
            }
        } else if (type.equals(MovieAdapter.TYPE.FAV)) {
            if (onUpdateMovieListListener != null) {
                onUpdateMovieListListener.onUpdateItemStarFav(movie.getId());
            }
        }
    }
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data");
                    if (bitmap != null) {
                        profileAvatar.setImageBitmap(bitmap);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                result.getData().getData()
                        );
                        profileAvatar.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
                    }
                }
            });
}
