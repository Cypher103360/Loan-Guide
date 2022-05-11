package com.instantloanguide.allloantips.activities;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import static com.instantloanguide.allloantips.fragments.StatusFragment.setImage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.instantloanguide.allloantips.R;
import com.instantloanguide.allloantips.activities.ui.main.SectionsPagerAdapter;
import com.instantloanguide.allloantips.databinding.ActivityHomeBinding;
import com.instantloanguide.allloantips.fragments.LoanTypeFragment;
import com.instantloanguide.allloantips.fragments.NewsFragment;
import com.instantloanguide.allloantips.fragments.StatusFragment;
import com.instantloanguide.allloantips.fragments.TipsFragment;
import com.instantloanguide.allloantips.utils.CommonMethods;
import com.instantloanguide.allloantips.utils.MyReceiver;
import com.instantloanguide.allloantips.utils.ShowAds;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.UnsupportedEncodingException;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String BroadCastStringForAction = "checkingInternet";
    private static final float END_SCALE = 0.7f;
    Dialog loading;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAnalytics mFirebaseAnalytics;
    Bundle bundle;
    String[] cameraPermission;
    String[] storagePermission;
    int count = 1;
    public static Uri uri;
    ImageView navMenu, headerImage;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ConstraintLayout categoryContainer;
    IntentFilter intentFilter;
    SectionsPagerAdapter sectionsPagerAdapter;
    private ActivityHomeBinding binding;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadCastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {

                    Set_Visibility_ON();
                    count++;
                } else {
                    Set_Visibility_OFF();
                }
            }
        }
    };

    private void Set_Visibility_ON() {
        binding.lottieHome.setVisibility(View.GONE);
        binding.tvNotConnected.setVisibility(View.GONE);
        binding.lottieContact.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);
        binding.tabs.setVisibility(View.VISIBLE);
        enableNavItems();
        if (count == 2) {
            ViewPager viewPager = binding.viewPager;
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = binding.tabs;
            tabs.setupWithViewPager(viewPager);
            navigationDrawer();
        }
    }

    private void Set_Visibility_OFF() {
        binding.lottieHome.setVisibility(View.VISIBLE);
        binding.tvNotConnected.setVisibility(View.VISIBLE);
        binding.lottieContact.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.GONE);
        binding.tabs.setVisibility(View.GONE);
        disableNavItems();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        loading = CommonMethods.getDialog(HomeActivity.this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        navigationView = binding.navigation;
        navMenu = binding.navMenu;
        bundle = new Bundle();
        drawerLayout = binding.drawerLayout;
        ShowAds showAds = new ShowAds(this, binding.topAdView, null);
        getLifecycle().addObserver(showAds);
        // allowing permissions of gallery and camera
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


        // Setting Version Code
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String version = pInfo.versionName;
            binding.versionCode.setText("Version : " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Internet checking Condition
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastStringForAction);
        Intent serviceIntent = new Intent(this, MyReceiver.class);
        startService(serviceIntent);
        if (isOnline(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Set_Visibility_ON();
            }
        } else {
            Set_Visibility_OFF();
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        sectionsPagerAdapter.addFragment(new LoanTypeFragment(), "Loan Types");
        sectionsPagerAdapter.addFragment(new NewsFragment(), "News");
        sectionsPagerAdapter.addFragment(new TipsFragment(), "Tips");
        sectionsPagerAdapter.addFragment(new StatusFragment(), "Status");


        binding.lottieContact.setOnClickListener(view -> {
            try {
                CommonMethods.whatsApp(HomeActivity.this);
            } catch (UnsupportedEncodingException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void navigationDrawer() {
        navigationView = findViewById(R.id.navigation);
        navigationView.bringToFront();
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(HomeActivity.this);
        navigationView.setCheckedItem(R.id.nav_home);
        categoryContainer = findViewById(R.id.container_layout);

        navMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        animateNavigationDrawer();
    }

    private void animateNavigationDrawer() {
        drawerLayout.setScrimColor(Color.parseColor("#DEE4EA"));
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                categoryContainer.setScaleX(offsetScale);
                categoryContainer.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = categoryContainer.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                categoryContainer.setTranslationX(xTranslation);
            }
        });
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                break;
            case R.id.nav_finance_tips:
                break;
            case R.id.nav_contact:
                try {
                    CommonMethods.whatsApp(HomeActivity.this);
                } catch (UnsupportedEncodingException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_rate:
                CommonMethods.rateApp(getApplicationContext());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Rate");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Rate click");
                mFirebaseAnalytics.logEvent("Selected_rate_menu_item", bundle);
                break;
            case R.id.nav_privacy:
                startActivity(new Intent(HomeActivity.this, PrivacyPolicyActivity.class));
                break;
            case R.id.nav_share:
                CommonMethods.shareApp(this);
                break;
            case R.id.nav_disclaimer:
                showDisclaimer();
                break;

            case R.id.nav_signOut:
                loading.show();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SignOut Menu");
                mFirebaseAnalytics.logEvent("Clicked_On_SignOut_Menu", bundle);
                // Sign Out for google user
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    googleSignOut();
                }
            default:
        }
        return true;
    }

    private void showDisclaimer() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else drawerLayout.openDrawer(GravityCompat.START);
        Dialog loadingDialog;
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.disclaimer_layout);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.item_bg2));
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        TextView textView = loadingDialog.findViewById(R.id.ok);
        textView.setOnClickListener(view -> loadingDialog.dismiss());
    }

    public void disableNavItems() {
        Menu navMenu = navigationView.getMenu();
        MenuItem nav_insta = navMenu.findItem(R.id.nav_share);
        nav_insta.setEnabled(false);

        MenuItem nav_policy = navMenu.findItem(R.id.nav_privacy);
        nav_policy.setEnabled(false);

        MenuItem nav_home = navMenu.findItem(R.id.nav_home);
        nav_home.setEnabled(false);

        MenuItem nav_rate = navMenu.findItem(R.id.nav_rate);
        nav_rate.setEnabled(false);

        MenuItem nav_contact = navMenu.findItem(R.id.nav_contact);
        nav_contact.setEnabled(false);

        MenuItem nav_signOut = navMenu.findItem(R.id.nav_signOut);
        nav_signOut.setEnabled(false);

        MenuItem nav_tips = navMenu.findItem(R.id.nav_finance_tips);
        nav_tips.setEnabled(false);
    }

    public void enableNavItems() {
        Menu navMenu = navigationView.getMenu();
        MenuItem nav_insta = navMenu.findItem(R.id.nav_share);
        nav_insta.setEnabled(true);

        MenuItem nav_policy = navMenu.findItem(R.id.nav_privacy);
        nav_policy.setEnabled(true);

        MenuItem nav_home = navMenu.findItem(R.id.nav_home);
        nav_home.setEnabled(true);

        MenuItem nav_rate = navMenu.findItem(R.id.nav_rate);
        nav_rate.setEnabled(true);

        MenuItem nav_contact = navMenu.findItem(R.id.nav_contact);
        nav_contact.setEnabled(true);

        MenuItem nav_signOut = navMenu.findItem(R.id.nav_signOut);
        nav_signOut.setEnabled(true);

        MenuItem nav_tips = navMenu.findItem(R.id.nav_finance_tips);
        nav_tips.setEnabled(true);
    }

    // Requesting camera and gallery
    // permission if not given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStomachache = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStomachache) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStomachache = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStomachache) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    // Here we will pick image from gallery or camera
    private void pickFromGallery() {
        CropImage.activity().start(this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                uri = result.getUri();
                setImage(uri, this);
                Glide.with(this).load(uri).into(StatusFragment.userImageView);
            }
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    public void googleSignOut() {
        gsc.signOut().addOnCompleteListener(task -> {
            finish();
            loading.dismiss();
            startActivity(new Intent(HomeActivity.this, SignUpActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do You Really Want To Exit?\nAlso Rate Us 5 Star.")
                .setNeutralButton("CANCEL", (dialog, which) -> {
                });


        builder.setNegativeButton("RATE APP", (dialog, which) -> CommonMethods.rateApp(getApplicationContext()))
                .setPositiveButton("OK!!", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    moveTaskToBack(true);
                    System.exit(0);

                });
        builder.show();
    }
}