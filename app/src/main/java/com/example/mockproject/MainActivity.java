package com.example.mockproject;

import static com.example.mockproject.utils.Constants.FRAGMENT_ABOUT;
import static com.example.mockproject.utils.Constants.FRAGMENT_FAVORITE;
import static com.example.mockproject.utils.Constants.FRAGMENT_MOVIE;
import static com.example.mockproject.utils.Constants.FRAGMENT_SETTING;
import static com.example.mockproject.utils.Constants.KEY_MOVIE_TYPE;
import static com.example.mockproject.utils.Constants.SHARE_KEY;
import static com.example.mockproject.utils.Constants.TYPE_NOW_PLAYING;
import static com.example.mockproject.utils.Constants.TYPE_POPULAR;
import static com.example.mockproject.utils.Constants.TYPE_TOP_RATED;
import static com.example.mockproject.utils.Constants.TYPE_UPCOMING;
import static com.example.mockproject.utils.Constants.USER_ID;

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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.callback.OnLoginRequestListener;
import com.example.mockproject.callback.OnOpenMovieDetailListener;
import com.example.mockproject.callback.OnUpdateMoviesListener;
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
import com.example.mockproject.adapter.ReminderAdapter;
import com.example.mockproject.utils.Utils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnLoginRequestListener, OnUpdateMoviesListener, OnOpenMovieDetailListener {

    private Fragment currentFragment;
    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    private FrameLayout detailFrameContainer, frameContainer;
    private ImageButton toolbarIconList, toolbarOptionsMenu, toolbarSearch, toolbarBack, toolbarBurger;
    private ImageView profileAvatar;
    private TextView profileUsername, profileEmail, profileBirthday, toolbarTitle, btnEdit, btnLogout, btnCancel, btnSave;
    private EditText toolbarSearchInput;
    private RadioButton isMale, isFemale;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean isSearching = false;
    private final String logText = "Main Activity";
    private MovieRepository movieRepository;
    private boolean isFromReminder = false;

    private void handleReminderIntentIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra("FROM_REMINDER", false)) {
            isFromReminder = true;
            int movieId = intent.getIntExtra("MOVIE_ID", 0);
            String movieTitle = intent.getStringExtra("MOVIE_TITLE");
            onOpenMovieDetail(movieId, movieTitle);

            intent.removeExtra("FROM_REMINDER");
            intent.removeExtra("MOVIE_ID");
            intent.removeExtra("MOVIE_TITLE");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userRepository = new UserRepository(MainActivity.this);
        sharedPreferences = getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().apply();
        detailFrameContainer = findViewById(R.id.detail_frame_container);
        frameContainer = findViewById(R.id.frame_container);
        movieRepository = new MovieRepository(MainActivity.this);

        setUpDrawer();
        setUpToolbar();
        setUpBottomNavAndVisibleToolbar();
        Intent intent = getIntent();
        handleReminderIntentIfNeeded(intent);
    }

    private void setUpDrawer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
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
            TextView btnLogin = headerView.findViewById(R.id.profile_btn_login);
            btnLogin.setOnClickListener(v -> {
                drawerLayout.closeDrawer(navigationView);
                showBottomDialog(MainActivity.this);
            });
        } else {
            loadDrawerData(headerView, userId);
        }
    }

    public void showBottomDialog(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.dialog_login);
        EditText inputEmail = bottomSheetDialog.findViewById(R.id.login_email);
        EditText inputUsername = bottomSheetDialog.findViewById(R.id.login_username);
        TextView btnSignIn = bottomSheetDialog.findViewById(R.id.login_submit);
        if(btnSignIn == null) return;

        btnSignIn.setOnClickListener(v -> {
            String email = (inputEmail != null ? inputEmail.getText().toString().trim() : "");
            String username = (inputUsername != null ? inputUsername.getText().toString().trim() : "");
            if (email.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "Please input email and username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Utils.isValidEmail(email)) {
                Toast.makeText(context, "Please input valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = userRepository.login(username, null, email, null, null);
            if (userId > 0) {
                sharedPreferences.edit().putString(USER_ID, String.valueOf(userId)).apply();
                Toast.makeText(context, "Login successfully!", Toast.LENGTH_SHORT).show();
                setUpDrawer();
                bottomSheetDialog.dismiss();

                FavoriteFragment favFragment = (FavoriteFragment) getOrCreateFragment(FavoriteFragment.class, FRAGMENT_FAVORITE);
                if (favFragment != null) {
                    favFragment.updateFavoriteList();
                }

                ListMoviesFragment listFragment = (ListMoviesFragment) getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
                if (listFragment != null) {
                    listFragment.refreshMovies();
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
        TextView btnShowReminder = headerView.findViewById(R.id.reminder_btn_show);

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
                Log.e(logText, Objects.requireNonNull(e.getMessage()));
            }
        }

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
        ReminderAdapter reminderAdapter = new ReminderAdapter(this,this, shortList);
        reminderRecycler.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        reminderRecycler.setAdapter(reminderAdapter);

        btnShowReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            reminderActivityLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sharedPreferences.edit().remove(USER_ID).apply();
            Toast.makeText(MainActivity.this, "Logout successfully!", Toast.LENGTH_SHORT).show();
            FavoriteFragment favFragment = (FavoriteFragment) getOrCreateFragment(FavoriteFragment.class, FRAGMENT_FAVORITE);
            if (favFragment != null) {
                favFragment.updateFavoriteUILogin();
            }
            setUpDrawer();
            ListMoviesFragment listFragment = (ListMoviesFragment) getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
            if (listFragment != null) {
                listFragment.refreshMovies();
            }
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
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                image = Base64.encodeToString(byteArray, Base64.DEFAULT);
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

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setVisibility(View.VISIBLE);
        toolbarSearchInput = findViewById(R.id.toolbar_search_input);
        toolbarSearchInput.setVisibility(View.GONE);
        toolbarSearch = findViewById(R.id.toolbar_icon_search);
        toolbarIconList = findViewById(R.id.toolbar_icon_list);
        toolbarOptionsMenu = findViewById(R.id.toolbar_icon_more);
        toolbarBack = findViewById(R.id.toolbar_icon_back);
        toolbarBurger = findViewById(R.id.toolbar_icon_burger);
        ImageButton toolbarIconClose = findViewById(R.id.toolbar_icon_close);
        toolbarIconClose.setVisibility(View.GONE);

        toolbarBurger.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        toolbarOptionsMenu.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, view);
            popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selectedType = "";
                if (item.getItemId() == R.id.op_menu_nowPlaying) {
                    selectedType = TYPE_NOW_PLAYING;
                } else if (item.getItemId() == R.id.op_menu_popular) {
                    selectedType = TYPE_POPULAR;
                } else if (item.getItemId() == R.id.op_menu_upcoming){
                    selectedType = TYPE_UPCOMING;
                } else if(item.getItemId() == R.id.op_menu_topRated){
                    selectedType = TYPE_TOP_RATED;
                }
                if (!selectedType.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
                    prefs.edit().putString(KEY_MOVIE_TYPE, selectedType).apply();

                    ListMoviesFragment listFragment = (ListMoviesFragment) getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
                    if (listFragment != null && listFragment.getView() != null) {
                        listFragment.refreshMovies();
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });

        toolbarIconList.setOnClickListener(v -> {
            ListMoviesFragment listFragment = (ListMoviesFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_MOVIE);
            if (listFragment != null && listFragment.getView() != null) {
                listFragment.toggleLayout();
                toolbarIconList.setImageResource(
                        listFragment.isGrid ? R.drawable.icon_toolbar_list : R.drawable.icon_toolbar_grid
                );
            }
        });


        toolbarSearch.setOnClickListener(view -> {
            if (!isSearching) {
                isSearching = true;
                toolbarTitle.setVisibility(View.GONE);
                toolbarSearchInput.setText("");
                toolbarSearchInput.setVisibility(View.VISIBLE);
                toolbarIconClose.setVisibility(View.VISIBLE);
                toolbarSearchInput.requestFocus();
            } else {
                String keyword = toolbarSearchInput.getText().toString().trim();
                doSearchFavorites(keyword);
            }
        });

        toolbarIconClose.setOnClickListener(view -> {
            isSearching = false;
            toolbarSearchInput.setText("");
            toolbarSearchInput.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
            toolbarIconClose.setVisibility(View.GONE);
            doSearchFavorites("");
        });
    }

    private void doSearchFavorites(String keyword) {
        String userIdStr = sharedPreferences.getString(USER_ID, "");
        if (userIdStr.isEmpty()) {
            return;
        }
        int userId = Integer.parseInt(userIdStr);

        List<Movie> searchResults = movieRepository.getFavMoviesByKeyword(userId, keyword);

        Fragment current = getSupportFragmentManager().findFragmentByTag(FRAGMENT_FAVORITE);
        if (current instanceof FavoriteFragment) {
            ((FavoriteFragment) current).showSearchResults(searchResults);
        }
    }

    private void setUpBottomNavAndVisibleToolbar() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        currentFragment = new ListMoviesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, currentFragment, FRAGMENT_MOVIE)
                .commit();
        updateFavoriteBadge();
        bottomNavigationView.setOnItemSelectedListener(item -> {
            setDetailDisplay(false);
            Fragment selectedFragment = null;
            String title = FRAGMENT_MOVIE;
            boolean isVisibleIconList = true;
            boolean isVisibleOpsMenu = true;
            boolean isVisibleSearch = false;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
                title = FRAGMENT_MOVIE;
            } else if (itemId == R.id.nav_favorite) {
                selectedFragment = getOrCreateFragment(FavoriteFragment.class, FRAGMENT_FAVORITE);
                title = FRAGMENT_FAVORITE;
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
                isVisibleSearch = true;
            } else if (itemId == R.id.nav_setting) {
                selectedFragment = getOrCreateFragment(SettingsFragment.class, FRAGMENT_SETTING);
                title = FRAGMENT_SETTING;
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            } else if (itemId == R.id.nav_about) {
                selectedFragment = getOrCreateFragment(AboutFragment.class, FRAGMENT_ABOUT);
                title = FRAGMENT_ABOUT;
                isVisibleIconList = false;
                isVisibleOpsMenu = false;
            }

            if (selectedFragment != null) {
                switchFragment(selectedFragment);
                toolbarTitle.setText(title);
                toolbarSearch.setVisibility(isVisibleSearch ? View.VISIBLE : View.GONE);
                toolbarIconList.setVisibility(isVisibleIconList ? View.VISIBLE : View.GONE);
                toolbarOptionsMenu.setVisibility(isVisibleOpsMenu ? View.VISIBLE : View.GONE);
            }
            return true;
        });
    }

    private void updateFavoriteBadge() {
        String userIdStr = sharedPreferences.getString(USER_ID, "");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        BadgeDrawable badgeDrawable = bottomNav.getOrCreateBadge(R.id.nav_favorite);
        if (userIdStr.isEmpty()) {
            badgeDrawable.clearNumber();
            badgeDrawable.setVisible(false);
            return;
        }
        int userId = Integer.parseInt(userIdStr);
        int favoriteCount = movieRepository.getFavMoviesByUserId(userId).size();
        if (favoriteCount > 0) {
            badgeDrawable.setNumber(favoriteCount);
            badgeDrawable.setVisible(true);
        } else {
            badgeDrawable.clearNumber();
            badgeDrawable.setVisible(false);
        }
    }

    private Fragment getOrCreateFragment(Class<? extends Fragment> fragmentClass, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            try {
                fragment = fragmentClass.newInstance();
            } catch (Exception e) {
                Log.e(logText, Objects.requireNonNull(e.getMessage()));
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

    public void reloadDrawerReminders() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            RecyclerView reminderRecycler = headerView.findViewById(R.id.reminder_short_list);
            ReminderRepository reminderRepository = new ReminderRepository(this);
            String userIdStr = sharedPreferences.getString(USER_ID, "0");
            if (userIdStr.isEmpty()) return;
            int userId = Integer.parseInt(userIdStr);
            List<Reminder> allReminders = reminderRepository.getRemindersByUser(userId);
            List<Reminder> shortList = (allReminders.size() > 3) ? allReminders.subList(0, 3) : allReminders;
            ReminderAdapter reminderAdapter = new ReminderAdapter(this,this, shortList);
            reminderRecycler.setAdapter(reminderAdapter);
        }
    }

    private final ActivityResultLauncher<Intent> reminderActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    reloadDrawerReminders();
                }
            });

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
                        Log.e(logText, Objects.requireNonNull(e.getMessage()));
                    }
                }
            });

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

    @Override
    public void onUpdateMoviesFromFavorite(Movie movie) {
        movieRepository.handleClickFavMovie(movie);
        updateFavoriteBadge();

        ListMoviesFragment listFragment = (ListMoviesFragment) getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
        if (listFragment != null) {
            listFragment.updateItemStarFav(movie.getId());
        }
    }

    @Override
    public void onUpdateMoviesFromList(Movie movie) {
        movieRepository.handleClickFavMovie(movie);
        updateFavoriteBadge();

        FavoriteFragment favFragment = (FavoriteFragment) getOrCreateFragment(FavoriteFragment.class, FRAGMENT_FAVORITE);
        if (favFragment != null) {
            favFragment.updateFavoriteList();
        }
    }

    @Override
    public void onUpdateMoviesFromDetail(Movie movie) {
        movieRepository.handleClickFavMovie(movie);
        updateFavoriteBadge();

        FavoriteFragment favFragment = (FavoriteFragment) getOrCreateFragment(FavoriteFragment.class, FRAGMENT_FAVORITE);
        if (favFragment != null) {
            favFragment.updateFavoriteList();
        }
        ListMoviesFragment listFragment = (ListMoviesFragment) getOrCreateFragment(ListMoviesFragment.class, FRAGMENT_MOVIE);
        if (listFragment != null) {
            listFragment.updateItemStarFav(movie.getId());
        }
    }

    @Override
    public void onOpenMovieDetail(int movieId, String movieTitle) {
        setDetailDisplay(true);

        MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movieId, movieTitle, MainActivity.this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_frame_container, detailFragment, "MovieDetail")
                .addToBackStack("Detail")
                .commit();
        toolbarTitle.setText(movieTitle);
        drawerLayout.closeDrawer(navigationView);
        toolbarBack.setOnClickListener(v -> {
            if (isFromReminder) {
                isFromReminder = false;
                finish();
            } else {
                setDetailDisplay(false);
                if (currentFragment instanceof ListMoviesFragment) {
                    toolbarTitle.setText(FRAGMENT_MOVIE);
                } else if (currentFragment instanceof FavoriteFragment) {
                    toolbarTitle.setText(FRAGMENT_ABOUT);
                }
            }
        });
    }
}
