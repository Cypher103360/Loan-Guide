package com.instantloanguide.allloantips.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.instantloanguide.allloantips.R;
import com.instantloanguide.allloantips.databinding.ActivityWelcomeBinding;
import com.instantloanguide.allloantips.models.ApiInterface;
import com.instantloanguide.allloantips.models.ApiWebServices;
import com.instantloanguide.allloantips.utils.CommonMethods;
import com.instantloanguide.allloantips.utils.ShowAds;
import com.ironsource.mediationsdk.IronSource;

import java.io.UnsupportedEncodingException;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;
    MaterialAlertDialogBuilder builder;
    ApiInterface apiInterface;
    //  Map<String,String> map = new HashMap<>();

    ShowAds showAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        AppLovinSdk.getInstance( this ).showMediationDebugger();
        showAds = new ShowAds();
        getLifecycle().addObserver(showAds);
//        AppLovinSdk.getInstance(this).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("145e514e-8593-4340-9233-fd54bc54c1e1"));


        apiInterface = ApiWebServices.getApiInterface();
        binding.startBtn.setOnClickListener(view -> {
            binding.lottieRunning.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                showAds.destroyBanner();
                showAds.showInterstitialAds(this);
                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                binding.lottieRunning.setVisibility(View.GONE);
            }, 3000);
        });

        binding.contactBtn.setOnClickListener(view -> {
            try {
                CommonMethods.whatsApp(this);
            } catch (UnsupportedEncodingException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        });

        binding.shareBtn.setOnClickListener(view -> CommonMethods.shareApp(this));

//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null) {
//            String name = account.getDisplayName();
//            String email = account.getEmail();
//            map.put("name", name);
//            map.put("email", email);
//            uploadUserData(map);
//        }
    }

//    private void uploadUserData(Map<String, String> map) {
//        Call<MessageModel> call = apiInterface.uploadUserData(map);
//        call.enqueue(new Callback<MessageModel>() {
//            @Override
//            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
//                if (response.isSuccessful()) {
//                    assert response.body() != null;
//                   // Toast.makeText(WelcomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
//            }
//        });
//    }

    @Override
    public void onBackPressed() {
        ShowExitDialog();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
        new Handler()
                .postDelayed(() -> {

                    showAds.showBottomBanner(this, findViewById(R.id.adView_bottom));
                    showAds.showTopBanner(this, findViewById(R.id.adView_top));
                }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    private void ShowExitDialog() {
        Dialog exitDialog = new Dialog(WelcomeActivity.this);
        exitDialog.setContentView(R.layout.exit_dialog_layout);
        exitDialog.getWindow().setLayout(600, ViewGroup.LayoutParams.WRAP_CONTENT);
        exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        exitDialog.setCancelable(false);
        exitDialog.show();

        TextView rateNow = exitDialog.findViewById(R.id.rate_now);
        TextView okBtn = exitDialog.findViewById(R.id.ok);
        ImageView cancelBtn = exitDialog.findViewById(R.id.dismiss_btn);

        cancelBtn.setOnClickListener(v -> {
            exitDialog.dismiss();
        });
        okBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            moveTaskToBack(true);
            System.exit(0);
        });

        rateNow.setOnClickListener(v -> {
            CommonMethods.rateApp(getApplicationContext());
        });


    }

}