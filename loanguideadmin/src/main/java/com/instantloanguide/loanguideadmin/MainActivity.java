package com.instantloanguide.loanguideadmin;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.instantloanguide.loanguideadmin.databinding.ActivityMainBinding;
import com.instantloanguide.loanguideadmin.databinding.AdLayoutLoanGuideBinding;
import com.instantloanguide.loanguideadmin.databinding.NewsCardLayoutBinding;
import com.instantloanguide.loanguideadmin.databinding.OwnTextUrlLayoutBinding;
import com.instantloanguide.loanguideadmin.databinding.UploadImageDialogBinding;
import com.instantloanguide.loanguideadmin.models.AdsModel;
import com.instantloanguide.loanguideadmin.models.BannerModel;
import com.instantloanguide.loanguideadmin.models.BannerModelList;
import com.instantloanguide.loanguideadmin.models.LoanAdsModel;
import com.instantloanguide.loanguideadmin.models.MessageModel;
import com.instantloanguide.loanguideadmin.models.OwnTextUrlModel;
import com.instantloanguide.loanguideadmin.models.UrlModel;
import com.instantloanguide.loanguideadmin.models.UrlModelList;
import com.instantloanguide.loanguideadmin.services.ApiInterface;
import com.instantloanguide.loanguideadmin.services.ApiWebServices;
import com.instantloanguide.loanguideadmin.utils.CommonMethods;
import com.instantloanguide.loanguideadmin.utils.FileUtils;
import com.instantloanguide.loanguideadmin.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    public static String key, adIdTitle;
    Dialog dialog, loadingDialog, stripBanDialog, adsUpdateDialog, bannerDialog, urlsDialog, loanGuideDialog;
    String hindiTitleTXt, engTitleTXt, url, englishTxt, hindiTxt;
    String encodedImage, banTitle, banImg, mUrls, mUrlTitle;
    Bitmap bitmap;
    Button uploadBannerBtn;
    ActivityResultLauncher<String> launcher;
    Map<String, String> map;
    ActivityMainBinding binding;
    NewsCardLayoutBinding cardLayoutBinding;
    UploadImageDialogBinding imageDialogBinding;
    ApiInterface apiInterface;
    Call<MessageModel> call;
    EditText admobKey, applovinAppKey, appOpenKey, admobBannerKey, nativeKey, banner, interstitial;
    ImageView okBtn, cancelBtn, bannerImageView;
    AdsModel adsModel;
    TextView title;
    Intent intent;
    AutoCompleteTextView networkName;
    //    String[] adsNetworks = {"AdmobWithMeta ", "IronSourceWithMeta", "AppLovinWithMeta", "Meta"};
    AdLayoutLoanGuideBinding loanGuideBinding;

    String[] items = new String[]{"AdmobWithMeta", "IronSourceWithMeta", "AppLovinWithMeta", "Meta"};
    String[] item2 = new String[]{"Native", "MREC"};
    AutoCompleteTextView BannerTopNetworkName, BannerBottomNetworkName, InterstitialNetwork, NativeAdsNetworkName, RewardAdsNetwork, nativeType;
    EditText AppId, AppLovinSdkKey, BannerTop, BannerBottom, InterstitialAds, NativeAds, rewardAds;
    Button UploadAdsBtn;
    Dialog loading;
    String appId, appLovinSdkKey, bannerTopNetworkName, bannerTop, bannerBottomNetworkName,
            bannerBottom, interstitialNetwork, interstitialAds, nativeAdsNetworkName,
            nativeAds, nativeAdsType, rewardAd, rewardAdsNetwork;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(binding.getRoot());
        loadingDialog = CommonMethods.loadingDialog(MainActivity.this);

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
                    encodedImage = imageStore(bitmap);
                    if (key.equals("news")) {
                        cardLayoutBinding.selectImage.setImageURI(result);
                    } else if (key.equals("banner")) {
                        Glide.with(this).load(result).into(bannerImageView);
                    } else imageDialogBinding.selectImage.setImageURI(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        handleClicks();

    }

    private void handleClicks() {
        binding.uploadNews.setOnClickListener(view -> {
            showNewsDialog("Upload News");
        });
        binding.uploadTips.setOnClickListener(view -> {
            showNewsDialog("Upload Tips");
        });
        binding.uploadStripImage.setOnClickListener(view -> {
            String[] items = new String[]{"Loan Types", "News", "Tips"};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle("Update Banners").setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0:
                        map.put("title", "loan_types");
                        updateBannerDialog(map);
                        break;
                    case 1:
                        map.put("title", "news");
                        updateBannerDialog(map);
                        break;
                    case 2:
                        map.put("title", "tips");
                        updateBannerDialog(map);
                        break;
                    default:
                }
            }).show();

        });
        binding.uploadAds.setOnClickListener(view -> {
            showUpdateLoanAdsDialog("LoanGuide");
        });
        binding.uploadAds2.setOnClickListener(view -> {
            showUpdateLoanAdsDialog("LoanGuide2");
        });
        binding.editBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, EditActivity.class));
        });

        binding.uploadLoanAppBtn.setOnClickListener(view -> {
            intent = new Intent(this, UploadLoanAppActivity.class);
            intent.putExtra("key", "upload");
            startActivity(intent);
        });
        binding.showLoanAppBtn.setOnClickListener(view -> {
            intent = new Intent(this, UploadLoanAppActivity.class);
            intent.putExtra("key", "show");
            startActivity(intent);
        });
        binding.urlBtn.setOnClickListener(v -> {
            String[] items = new String[]{"Financial Tips", "EMI Calculator"};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle("Update Urls").setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0:
                        loadingDialog.show();
                        map.put("title", "finance_tips");
                        updateUrlsDialog(map, "Financial Tips");
                        break;
                    case 1:
                        loadingDialog.show();
                        map.put("title", "emi_cal");
                        updateUrlsDialog(map, "EMI Calculator");
                        break;
                    default:
                }
            }).show();
        });
        binding.userDataBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserDataActivity.class));
        });

        binding.updateOwnTextBtn.setOnClickListener(view -> {
            updateOwnTextUrl();
        });
    }

    private void updateOwnTextUrl() {
        Dialog ownTextDialog = new Dialog(this);
        OwnTextUrlLayoutBinding ownTextUrlLayoutBinding = OwnTextUrlLayoutBinding.inflate(getLayoutInflater());
        ownTextDialog.setContentView(ownTextUrlLayoutBinding.getRoot());
        ownTextDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ownTextDialog.setCancelable(false);
        ownTextDialog.show();

        EditText titleEd = ownTextUrlLayoutBinding.titleEdit;
        EditText urlEd = ownTextUrlLayoutBinding.urEdit;
        Button cancelBtn, uploadUrlsBtn;
        cancelBtn = ownTextUrlLayoutBinding.cancelBtn;
        cancelBtn.setOnClickListener(v -> {
            ownTextDialog.dismiss();
        });
        uploadUrlsBtn = ownTextUrlLayoutBinding.uploadUrls;

        Call<OwnTextUrlModel> call = apiInterface.fetchOwnText();
        call.enqueue(new Callback<OwnTextUrlModel>() {
            @Override
            public void onResponse(@NonNull Call<OwnTextUrlModel> call, @NonNull Response<OwnTextUrlModel> response) {
                if (response.isSuccessful()) {

                    urlEd.setText(Objects.requireNonNull(response.body()).getUrl());
                    titleEd.setText(response.body().getTitle());
                    loadingDialog.dismiss();

                }
            }

            @Override
            public void onFailure(@NonNull Call<OwnTextUrlModel> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
            }
        });

        uploadUrlsBtn.setOnClickListener(v -> {
            loadingDialog.show();
            String url = urlEd.getText().toString().trim();
            String title = titleEd.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                titleEd.setError("Url Required");
                titleEd.requestFocus();
            } else if (TextUtils.isEmpty(url)) {
                urlEd.setError("Url Required");
                urlEd.requestFocus();
            } else {
                map.put("title", title);
                map.put("url", url);
                Call<MessageModel> modelCall = apiInterface.updateOwnText(map);
                modelCall.enqueue(new Callback<MessageModel>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                            ownTextDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                        loadingDialog.dismiss();
                    }
                });
            }
        });

    }

    private void updateUrlsDialog(Map<String, String> map, String title) {
        urlsDialog = new Dialog(MainActivity.this);
        urlsDialog.setContentView(R.layout.url_layout);
        urlsDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        urlsDialog.setCancelable(false);
        urlsDialog.show();

        TextView dialogTitle = urlsDialog.findViewById(R.id.dialog_title);
        dialogTitle.setText(title);
        EditText urlEd = urlsDialog.findViewById(R.id.ur_edit);
        Button cancelBtn, uploadUrlsBtn;
        cancelBtn = urlsDialog.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(v -> {
            urlsDialog.dismiss();
        });
        uploadUrlsBtn = urlsDialog.findViewById(R.id.upload_urls);

        Call<UrlModelList> call = apiInterface.fetchUrls(map);
        call.enqueue(new Callback<UrlModelList>() {
            @Override
            public void onResponse(@NonNull Call<UrlModelList> call, @NonNull Response<UrlModelList> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getData() != null) {
                        loadingDialog.dismiss();
                        for (UrlModel urlModel : response.body().getData()) {
                            urlEd.setText(urlModel.getUrl());
                            mUrlTitle = urlModel.getTitle();
                            loadingDialog.dismiss();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UrlModelList> call, @NonNull Throwable t) {

            }
        });

        uploadUrlsBtn.setOnClickListener(v -> {
            loadingDialog.show();
            String url = urlEd.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                urlEd.setError("Url Required");
                urlEd.requestFocus();
            } else {
                map.put("title", mUrlTitle);
                map.put("url", url);
                updateUrls(map);
            }
        });

    }

    private void updateUrls(Map<String, String> map) {
        Call<MessageModel> call = apiInterface.updateUrls(map);
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                    urlsDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {

            }
        });
    }

    private void updateBannerDialog(Map<String, String> map) {
        bannerDialog = new Dialog(MainActivity.this);
        bannerDialog.setContentView(R.layout.upload_banner_layout);
        bannerDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bannerDialog.setCancelable(false);
        bannerDialog.show();

        bannerImageView = bannerDialog.findViewById(R.id.selectBannerImage);
        EditText edBannerUrl = bannerDialog.findViewById(R.id.banner_url);
        uploadBannerBtn = bannerDialog.findViewById(R.id.upload_banner_btn);
        Button cancelBtn = bannerDialog.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(view -> {
            bannerDialog.dismiss();
            encodedImage = "";
        });

        bannerImageView.setOnClickListener(v -> {
//            launcher.launch("image/*");
//            key = "banner";
            requestPermission();
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 102);
        });

        Call<BannerModelList> call = apiInterface.fetchBanner(map);
        call.enqueue(new Callback<BannerModelList>() {
            @Override
            public void onResponse(@NonNull Call<BannerModelList> call, @NonNull Response<BannerModelList> response) {

                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getData() != null) {

                        for (BannerModel ban : response.body().getData()) {
                            Glide.with(MainActivity.this).load("https://gedgetsworld.in/Loan_App/strip_banner_images/" + ban.getImage()).into(bannerImageView);
                            edBannerUrl.setText(ban.getUrl());
                            banTitle = ban.getTitle();
                            banImg = ban.getImage();
                            encodedImage = banImg;
                            loadingDialog.dismiss();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BannerModelList> call, @NonNull Throwable t) {

            }
        });


        uploadBannerBtn.setOnClickListener(view -> {
            loadingDialog.show();
            Log.d("checkEncodedImg", encodedImage + banTitle);

            String url = edBannerUrl.getText().toString().trim();

            if (encodedImage.equals(banImg)) {

                MultipartBody.Part imgPart = MultipartBody.Part.createFormData("img", encodedImage);
                MultipartBody.Part idPart = MultipartBody.Part.createFormData("title", banTitle);
                MultipartBody.Part urlPart = MultipartBody.Part.createFormData("url", url);
                MultipartBody.Part deleteImgPart = MultipartBody.Part.createFormData("deleteImg", banImg);
                MultipartBody.Part imgKeyPart = MultipartBody.Part.createFormData("imgKey", "0");
                Call<MessageModel> call1 = apiInterface.updateBanner(imgPart, idPart, urlPart, deleteImgPart, imgKeyPart);

                updateBannerData(call1);
            } else {
                File imgFile = new File(Uri.parse(encodedImage).getPath());
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);

                MultipartBody.Part imgPart = MultipartBody.Part.createFormData("img", imgFile.getName(), requestBody);
                MultipartBody.Part idPart = MultipartBody.Part.createFormData("title", banTitle);
                MultipartBody.Part urlPart = MultipartBody.Part.createFormData("url", url);
                MultipartBody.Part deleteImgPart = MultipartBody.Part.createFormData("deleteImg", banImg);
                MultipartBody.Part imgKeyPart = MultipartBody.Part.createFormData("imgKey", "1");
                Call<MessageModel> call1 = apiInterface.updateBanner(imgPart, idPart, urlPart, deleteImgPart, imgKeyPart);
                updateBannerData(call1);


            }
        });


    }

    private void updateBannerData(Call<MessageModel> call) {
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    bannerDialog.dismiss();
                } else {
                    assert response.body() != null;
                    Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Check Error", t.getMessage());

                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            encodedImage = FileUtils.getPath(this, uri);
            Glide.with(this).load(uri).into(bannerImageView);
        }
    }


    private void init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        loadingDialog = Util.showDialog(this);
        map = new HashMap<>();
        apiInterface = ApiWebServices.getApiInterface();

    }

    private void showNewsDialog(String dialogName) {

        dialog = new Dialog(this);
        cardLayoutBinding = NewsCardLayoutBinding.inflate(getLayoutInflater());
        dialog.setContentView(cardLayoutBinding.getRoot());
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ads_item_bg));
        dialog.setCancelable(false);
        dialog.show();
        if (dialogName.equals("Upload Tips")) {
            cardLayoutBinding.selectImage.setVisibility(View.GONE);
        } else
            cardLayoutBinding.selectImage.setVisibility(View.VISIBLE);

        cardLayoutBinding.selectImage.setOnClickListener(v -> {
            launcher.launch("image/*");
            key = "news";
        });
        cardLayoutBinding.backBtn.setOnClickListener(view -> dialog.cancel());

        cardLayoutBinding.title.setText(dialogName);

        cardLayoutBinding.okBtn.setOnClickListener(view -> {
            hindiTitleTXt = cardLayoutBinding.itemTitle.getText().toString();
            engTitleTXt = cardLayoutBinding.itemEngTitle.getText().toString();
            url = cardLayoutBinding.itemUrl.getText().toString();
            englishTxt = cardLayoutBinding.englishDesc.getText().toString();
            hindiTxt = cardLayoutBinding.hindiDesc.getText().toString();

            if (dialogName.equals("Upload Tips")) {

                if (TextUtils.isEmpty(hindiTitleTXt)) {
                    cardLayoutBinding.itemTitle.setError("Title required!");

                }
                if (TextUtils.isEmpty(engTitleTXt)) {
                    cardLayoutBinding.itemEngTitle.setError("Title required!");

                }
                if (TextUtils.isEmpty(url)) {
                    cardLayoutBinding.itemUrl.setError("Title required!");

                } else if (TextUtils.isEmpty(englishTxt)) {
                    cardLayoutBinding.englishDesc.setError("Field required!");

                } else if (TextUtils.isEmpty(hindiTxt)) {
                    cardLayoutBinding.hindiDesc.setError("Field required!");
                } else {
                    loadingDialog.show();
                    map.put("title", hindiTitleTXt);
                    map.put("engTitle", engTitleTXt);
                    map.put("url", url);
                    map.put("englishDesc", englishTxt);
                    map.put("hindiDesc", hindiTxt);
                    uploadData(map, "Upload Tips");

                }
            } else {
                if (encodedImage == null) {
                    Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();

                }
                if (TextUtils.isEmpty(hindiTitleTXt)) {
                    cardLayoutBinding.itemTitle.setError("Title required!");

                }
                if (TextUtils.isEmpty(engTitleTXt)) {
                    cardLayoutBinding.itemEngTitle.setError("Title required!");

                }
                if (TextUtils.isEmpty(url)) {
                    cardLayoutBinding.itemUrl.setError("Title required!");

                } else if (TextUtils.isEmpty(englishTxt)) {
                    cardLayoutBinding.englishDesc.setError("Field required!");

                } else if (TextUtils.isEmpty(hindiTxt)) {
                    cardLayoutBinding.hindiDesc.setError("Field required!");
                } else {
                    loadingDialog.show();
                    map.put("img", encodedImage);
                    map.put("title", hindiTitleTXt);
                    map.put("engTitle", engTitleTXt);
                    map.put("url", url);
                    map.put("englishDesc", englishTxt);
                    map.put("hindiDesc", hindiTxt);
                    uploadData(map, "Upload News");
                }
            }


        });


    }

    private void uploadData(Map<String, String> map, String key) {
        if (key.equals("Upload News")) {
            call = apiInterface.uploadNews(map);
        } else if (key.equals("Upload Tips")) {
            call = apiInterface.uploadTips(map);
        } else if (key.equals("Strip Banner")) {
            call = apiInterface.uploadStripBan(map);
        }
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {


                if (response.isSuccessful()) {
                    loadingDialog.dismiss();
                    if (key.equals("Strip Banner")) {
                        stripBanDialog.dismiss();
                    }
                    dialog.dismiss();
                    init();
                    Toast.makeText(MainActivity.this, "Data Uploaded", Toast.LENGTH_SHORT).show();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, t.getMessage());
            }
        });
    }

//    private void showUpdateAdsDialog(String key) {
//        adsUpdateDialog = new Dialog(this);
//        adsUpdateDialog.setContentView(R.layout.ad_id_layout);
//        adsUpdateDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        adsUpdateDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ads_item_bg));
//        adsUpdateDialog.setCancelable(false);
//        adsUpdateDialog.show();
//        admobKey = adsUpdateDialog.findViewById(R.id.admobKey);
//        applovinAppKey = adsUpdateDialog.findViewById(R.id.appLovin_app_key);
//        appOpenKey = adsUpdateDialog.findViewById(R.id.app_open_id);
//        admobBannerKey = adsUpdateDialog.findViewById(R.id.admob_banner_id);
//        nativeKey = adsUpdateDialog.findViewById(R.id.native_ads_id);
//        banner = adsUpdateDialog.findViewById(R.id.banner_ads_id);
//        interstitial = adsUpdateDialog.findViewById(R.id.interstitial_ads_id);
//        networkName = adsUpdateDialog.findViewById(R.id.networkName);
//        okBtn = adsUpdateDialog.findViewById(R.id.ok_btn);
//        cancelBtn = adsUpdateDialog.findViewById(R.id.back_btn);
//        title = adsUpdateDialog.findViewById(R.id.title);
//
//        cancelBtn.setOnClickListener(v -> {
//            adsUpdateDialog.dismiss();
//        });
//
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adsNetworks);
//        networkName.setAdapter(adapter);
//        networkName.setThreshold(1);
//        apiInterface = ApiWebServices.getApiInterface();
//        Call<AdsModelList> call = apiInterface.fetchAds(key);
//        call.enqueue(new Callback<AdsModelList>() {
//            @Override
//            public void onResponse(@NonNull Call<AdsModelList> call, @NonNull Response<AdsModelList> response) {
//                if (response.isSuccessful()) {
//                    if (Objects.requireNonNull(response.body()).getData() != null) {
//                        for (AdsModel ads : response.body().getData()) {
//                            adsModel = ads;
//                            adIdTitle = ads.getId();
//                            title.setText(adIdTitle);
//                            admobKey.setText(ads.getAdmobAppKey());
//                            applovinAppKey.setText(ads.getAppLovinAppKey());
//                            appOpenKey.setText(ads.getAppOpen());
//                            admobBannerKey.setText(ads.getAdmobBanner());
//                            nativeKey.setText(ads.getNativeADs());
//                            banner.setText(ads.getBanner());
//                            interstitial.setText(ads.getInterstitial());
//                            networkName.setText(ads.getNetworkName());
//                        }
//                    }
//                } else {
//                    Log.d("adsError", response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AdsModelList> call, @NonNull Throwable t) {
//                Log.d("adsError", t.getMessage());
//            }
//        });
//
//
//        okBtn.setOnClickListener(v -> {
//            String admobId = admobKey.getText().toString().trim();
//            String ironSourceId = applovinAppKey.getText().toString().trim();
//            String appOpenId = appOpenKey.getText().toString().trim();
//            String admobBanner = admobBannerKey.getText().toString().trim();
//            String nativeId = nativeKey.getText().toString().trim();
//            String bannerId = banner.getText().toString().trim();
//            String interstitialId = interstitial.getText().toString().trim();
//            String network = networkName.getText().toString().trim();
//            if (admobId.equals(adsModel.getAdmobAppKey())
//
//                    && ironSourceId.equals(adsModel.getAppLovinAppKey())
//                    && appOpenId.equals(adsModel.getAppOpen())
//                    && admobBanner.equals(adsModel.getAdmobBanner())
//                    && nativeId.equals(adsModel.getNativeADs())
//                    && bannerId.equals(adsModel.getBanner())
//                    && interstitialId.equals(adsModel.getInterstitial())
//                    && network.equals(adsModel.getNetworkName())
//
//            ) {
//
//                Toast.makeText(this, "No changes made in Ids", Toast.LENGTH_SHORT).show();
//
//            } else {
//
//
//                loadingDialog.show();
//                map.put("id", adIdTitle);
//                map.put("admobAppKey", admobId);
//                map.put("appLovinAppKey", ironSourceId);
//                map.put("appOpen", appOpenId);
//                map.put("admobBanner", admobBanner);
//                map.put("native", nativeId);
//                map.put("banner", bannerId);
//                map.put("interstitial", interstitialId);
//                map.put("networkName", network);
//                updateAdIds(map);
//            }
//        });
//    }

//    private void updateAdIds(Map<String, String> map) {
//        Call<MessageModel> call = apiInterface.updateAdIds(map);
//        call.enqueue(new Callback<MessageModel>() {
//            @Override
//            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
//                if (response.isSuccessful()) {
//                    assert response.body() != null;
//                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//                loadingDialog.dismiss();
//                adsUpdateDialog.dismiss();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
//                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                loadingDialog.dismiss();
//            }
//        });
//    }

    private void showUpdateLoanAdsDialog(String id) {

        loanGuideDialog = new Dialog(this);
        loanGuideBinding = AdLayoutLoanGuideBinding.inflate(getLayoutInflater());
        loanGuideDialog.setContentView(loanGuideBinding.getRoot());
        loanGuideDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        loanGuideDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.item_bg));
        loanGuideDialog.setCancelable(false);
        loanGuideDialog.show();

        loanGuideBinding.textView.setText(id);

        loanGuideBinding.cancelId.setOnClickListener(v -> loanGuideDialog.dismiss());
        BannerTopNetworkName = loanGuideBinding.bannerTopNetworkName;
        BannerBottomNetworkName = loanGuideBinding.bannerBottomNetworkName;
        InterstitialNetwork = loanGuideBinding.interstitialNetwork;
        NativeAdsNetworkName = loanGuideBinding.nativeAdsNetworkName;
        RewardAdsNetwork = loanGuideBinding.rewardAdsNetwork;
        UploadAdsBtn = loanGuideBinding.uploadIds;

        AppId = loanGuideBinding.appId;
        AppLovinSdkKey = loanGuideBinding.appLovinSdkKey;
        BannerTop = loanGuideBinding.bannerTop;
        BannerBottom = loanGuideBinding.bannerBottom;
        InterstitialAds = loanGuideBinding.interstitialAds;
        NativeAds = loanGuideBinding.nativeAds;
        nativeType = loanGuideBinding.nativeAdsType;
        rewardAds = loanGuideBinding.rewardAds;

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                androidx.databinding.library.baseAdapters.R.layout.support_simple_spinner_dropdown_item, items);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(MainActivity.this,
                androidx.databinding.library.baseAdapters.R.layout.support_simple_spinner_dropdown_item, item2);
        nativeType.setAdapter(arrayAdapter2);
        BannerTopNetworkName.setAdapter(arrayAdapter);
        BannerBottomNetworkName.setAdapter(arrayAdapter);
        InterstitialNetwork.setAdapter(arrayAdapter);
        NativeAdsNetworkName.setAdapter(arrayAdapter);
        RewardAdsNetwork.setAdapter(arrayAdapter);

        apiInterface = ApiWebServices.getApiInterface();
        Call<List<LoanAdsModel>> call = apiInterface.fetchLoanAds(id);
        call.enqueue(new Callback<List<LoanAdsModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LoanAdsModel>> call, @NonNull Response<List<LoanAdsModel>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        for (LoanAdsModel ads : response.body()) {
                            AppId.setText(ads.getAppId());
                            AppLovinSdkKey.setText(ads.getAppLovinAppKey());
                            BannerTopNetworkName.setText(ads.getBannerTopAdNetwork());
                            BannerTop.setText(ads.getBannerTop());
                            BannerBottomNetworkName.setText(ads.getBannerBottomAdNetwork());
                            BannerBottom.setText(ads.getBannerBottom());
                            InterstitialNetwork.setText(ads.getInterstitalAdNetwork());
                            InterstitialAds.setText(ads.getInterstitial());
                            NativeAdsNetworkName.setText(ads.getNativeAdNetwork());
                            NativeAds.setText(ads.getNativeAd());
                            nativeType.setText(ads.getNativeType());
                            RewardAdsNetwork.setText(ads.getRewardAdNetwork());
                            rewardAds.setText(ads.getRewardAd());

                        }
                    }
                } else {
                    Log.e("adsError", response.message());
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<LoanAdsModel>> call, @NonNull Throwable t) {
                Log.d("adsError", t.getMessage());
            }
        });


        UploadAdsBtn.setOnClickListener(view -> {
            appId = AppId.getText().toString().trim();
            appLovinSdkKey = AppLovinSdkKey.getText().toString().trim();
            bannerTopNetworkName = BannerTopNetworkName.getText().toString().trim();
            bannerTop = BannerTop.getText().toString().trim();
            bannerBottomNetworkName = BannerBottomNetworkName.getText().toString().trim();
            bannerBottom = BannerBottom.getText().toString().trim();
            interstitialNetwork = InterstitialNetwork.getText().toString().trim();
            interstitialAds = InterstitialAds.getText().toString().trim();
            nativeAdsNetworkName = NativeAdsNetworkName.getText().toString().trim();
            nativeAds = NativeAds.getText().toString().trim();
            nativeAdsType = nativeType.getText().toString().trim();
            rewardAdsNetwork = RewardAdsNetwork.getText().toString().trim();
            rewardAd = rewardAds.getText().toString().trim();

            if (TextUtils.isEmpty(appId)) {
                AppId.setError("App id is required");
                AppId.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(appLovinSdkKey)) {
                AppLovinSdkKey.setError("AppLovinSdkKey is required");
                AppLovinSdkKey.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(bannerTopNetworkName)) {
                BannerTopNetworkName.setError("BannerTopNetworkName is required");
                BannerTopNetworkName.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(bannerTop)) {
                BannerTop.setError("BannerTop is required");
                BannerTop.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(bannerBottomNetworkName)) {
                BannerBottomNetworkName.setError("BannerBottomNetworkName is required");
                BannerBottomNetworkName.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(bannerBottom)) {
                BannerBottom.setError("BannerBottom is required");
                BannerBottom.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(interstitialNetwork)) {
                InterstitialNetwork.setError("InterstitialNetwork is required");
                InterstitialNetwork.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(interstitialAds)) {
                InterstitialAds.setError("InterstitialAds is required");
                InterstitialAds.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(nativeAdsNetworkName)) {
                NativeAdsNetworkName.setError("NativeAdsNetworkName is required");
                NativeAdsNetworkName.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(nativeAds)) {
                NativeAds.setError("NativeAds is required");
                NativeAds.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(nativeAdsType)) {
                nativeType.setError("NativeType is required");
                nativeType.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(rewardAdsNetwork)) {
                RewardAdsNetwork.setError("rewardAdsNetwork is required");
                RewardAdsNetwork.requestFocus();
                loading.dismiss();
            } else if (TextUtils.isEmpty(rewardAd)) {
                rewardAds.setError("rewardAd is required");
                rewardAds.requestFocus();
                loading.dismiss();
            } else {
                loadingDialog.show();
                map.put("id", id);
                map.put("appId", appId);
                map.put("appLovinSdkKey", appLovinSdkKey);
                map.put("bannerTop", bannerTop);
                map.put("bannerTopNetworkName", bannerTopNetworkName);
                map.put("bannerBottom", bannerBottom);
                map.put("bannerBottomNetworkName", bannerBottomNetworkName);
                map.put("interstitialAds", interstitialAds);
                map.put("interstitialNetwork", interstitialNetwork);
                map.put("nativeAds", nativeAds);
                map.put("nativeAdsNetworkName", nativeAdsNetworkName);
                map.put("nativeType", nativeAdsType);
                map.put("rewardAd", rewardAd);
                map.put("rewardAdNetwork", rewardAdsNetwork);
                updateLoanAdIds(map);
            }

        });

    }

    private void updateLoanAdIds(Map<String, String> map) {
        Call<MessageModel> call = apiInterface.updateLoanAdIds(map);
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
                loanGuideDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    public String imageStore(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }
}