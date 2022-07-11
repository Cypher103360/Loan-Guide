package com.instantloanguide.allloantips.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.instantloanguide.allloantips.activities.IncomeResourceActivity;
import com.instantloanguide.allloantips.databinding.FragmentLoanTypeBinding;
import com.instantloanguide.allloantips.models.ApiInterface;
import com.instantloanguide.allloantips.models.ApiWebServices;
import com.instantloanguide.allloantips.models.BannerModel;
import com.instantloanguide.allloantips.models.BannerModelList;
import com.instantloanguide.allloantips.models.UrlModel;
import com.instantloanguide.allloantips.models.UrlModelList;
import com.instantloanguide.allloantips.utils.Ads;
import com.instantloanguide.allloantips.utils.CommonMethods;
import com.instantloanguide.allloantips.utils.Prevalent;
import com.instantloanguide.allloantips.utils.ShowAds;
import com.ironsource.mediationsdk.IronSource;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoanTypeFragment extends Fragment {

    FragmentLoanTypeBinding binding;
    ShowAds showAds = new ShowAds();
    ApiInterface apiInterface;
    Dialog loading;
    String banUrl, emiCalUrl;
    Map<String, String> map = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoanTypeBinding.inflate(inflater, container, false);
        loading = CommonMethods.getDialog(requireContext());
        loading.show();
        apiInterface = ApiWebServices.getApiInterface();
        fetchBannerImages();
        fetchTipsUrl();


        if (Paper.book().read(Prevalent.bannerTopNetworkName).equals("IronSourceWithMeta")) {
            binding.adViewTop.setVisibility(View.GONE);
            showAds.showBottomBanner(requireActivity(), binding.adViewBottom);

        } else if (Paper.book().read(Prevalent.bannerBottomNetworkName).equals("IronSourceWithMeta")) {
            binding.adViewBottom.setVisibility(View.GONE);
            showAds.showTopBanner(requireActivity(), binding.adViewTop);

        } else {
            showAds.showTopBanner(requireActivity(), binding.adViewTop);
            showAds.showBottomBanner(requireActivity(), binding.adViewBottom);
        }
        binding.loanOrPersonal.setOnClickListener(view -> {
            showAds.showInterstitialAds(requireActivity());
            showAds.destroyBanner();
            startActivity(new Intent(requireContext(), IncomeResourceActivity.class));
        });
        binding.anyOtherTypeLoan.setOnClickListener(view -> {
            showAds.showInterstitialAds(requireActivity());

            showAds.destroyBanner();
            startActivity(new Intent(requireContext(), IncomeResourceActivity.class));
        });

        binding.helpAndSupport.setOnClickListener(view -> {
            try {
                CommonMethods.whatsApp(requireContext());
            } catch (UnsupportedEncodingException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        });
        binding.textView15.setText(Paper.book().read(Prevalent.title));
        binding.ownTextUrl.setOnClickListener(view -> {
            openWebPage(Paper.book().read(Prevalent.url));
        });
        binding.emiCalculator.setOnClickListener(view -> {
            openWebPage(emiCalUrl);
//            ArrayList<HashMap<String, Object>> items = new ArrayList<>();
//
//            final PackageManager pm = requireActivity().getPackageManager();
//            List<PackageInfo> packs = pm.getInstalledPackages(0);
//            for (PackageInfo pi : packs) {
//                if (pi.packageName.toLowerCase().contains("calcul")) {
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("appName", pi.applicationInfo.loadLabel(pm));
//                    map.put("packageName", pi.packageName);
//                    items.add(map);
//                }
//            }
//            if (items.size() >= 1) {
//                String packageName = (String) items.get(0).get("packageName");
//                Intent i = pm.getLaunchIntentForPackage(packageName);
//                if (i != null)
//                    startActivity(i);
//            } else {
//                // Application not found
//            }
        });

        return binding.getRoot();
    }

    private void fetchTipsUrl() {
        map.put("title", "emi_cal");
        Call<UrlModelList> call = apiInterface.fetchUrls(map);
        call.enqueue(new Callback<UrlModelList>() {
            @Override
            public void onResponse(@NonNull Call<UrlModelList> call, @NonNull Response<UrlModelList> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getData() != null) {
                        for (UrlModel urlModel : response.body().getData()) {
                            emiCalUrl = urlModel.getUrl();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UrlModelList> call, @NonNull Throwable t) {

            }
        });
    }

    public void fetchBannerImages() {
        map.put("title", "loan_types");
        Call<BannerModelList> call = apiInterface.fetchBanner(map);
        call.enqueue(new Callback<BannerModelList>() {
            @Override
            public void onResponse(@NonNull Call<BannerModelList> call, @NonNull Response<BannerModelList> response) {

                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getData() != null) {

                        for (BannerModel ban : response.body().getData()) {
                            Glide.with(requireActivity()).load("https://gedgetsworld.in/Loan_App/strip_banner_images/"
                                    + ban.getImage()).into(binding.loanTypeBannerImageView);

                            banUrl = ban.getUrl();
//                            loading.dismiss();

                        }
                        binding.loanTypeBannerImageView.setOnClickListener(v -> {
                            openWebPage(banUrl);
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BannerModelList> call, @NonNull Throwable t) {

            }
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IronSource.onResume(requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        IronSource.onPause(requireActivity());
    }
}