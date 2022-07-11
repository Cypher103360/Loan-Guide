package com.instantloanguide.loanguideadmin;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.instantloanguide.loanguideadmin.adapters.LoanAppsAdapter;
import com.instantloanguide.loanguideadmin.databinding.ActivityUploadLoanAppBinding;
import com.instantloanguide.loanguideadmin.databinding.EditLoanAppDataLayoutBinding;
import com.instantloanguide.loanguideadmin.databinding.LoanAppLayoutBinding;
import com.instantloanguide.loanguideadmin.models.LoanAppModel;
import com.instantloanguide.loanguideadmin.models.LoanAppModelList;
import com.instantloanguide.loanguideadmin.models.MessageModel;
import com.instantloanguide.loanguideadmin.services.ApiInterface;
import com.instantloanguide.loanguideadmin.services.ApiWebServices;
import com.instantloanguide.loanguideadmin.utils.CommonMethods;
import com.instantloanguide.loanguideadmin.viewModels.LoanAppViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadLoanAppActivity extends AppCompatActivity implements LoanAppsAdapter.LoanAppInterface {

    ActivityUploadLoanAppBinding binding;
    LoanAppLayoutBinding layoutBinding;
    EditLoanAppDataLayoutBinding loanAppDataLayoutBinding;
    Dialog dialog, loadingDialog, loanAppDataLayoutDialog;
    String encodedImage, key;
    ActivityResultLauncher<String> launcher;
    Bitmap bitmap;
    Map<String, String> map;
    ApiInterface apiInterface;
    List<LoanAppModel> loanAppModels = new ArrayList<>();
    LoanAppsAdapter loanAppsAdapter;
    LoanAppViewModel loanAppViewModel;
    ItemTouchHelper.SimpleCallback simpleCallback;
    MaterialAlertDialogBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadLoanAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = CommonMethods.loadingDialog(this);
        key = getIntent().getStringExtra("key");


        binding.uploadPersonalLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("personal_loan");
            else if (key.equals("show"))
                ShowLoanAppData("personal_loan");
        });
        binding.uploadBankLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("bank_loan");
            else if (key.equals("show"))
                ShowLoanAppData("bank_loan");
        });
        binding.uploadBusinessLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("business_loan");
            else if (key.equals("show"))
                ShowLoanAppData("business_loan");
        });
        binding.uploadHomeLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("home_loan");
            else if (key.equals("show"))
                ShowLoanAppData("home_loan");
        });
        binding.uploadAadharLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("aadhar_loan");
            else if (key.equals("show"))
                ShowLoanAppData("aadhar_loan");
        });
        binding.uploadStudentLoan.setOnClickListener(view -> {
            if (key.equals("upload"))
                UploadLoanDataDialog("student_loan");
            else if (key.equals("show"))
                ShowLoanAppData("student_loan");
        });

        map = new HashMap<>();
        apiInterface = ApiWebServices.getApiInterface();

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
                    encodedImage = imageStore(bitmap);
                    layoutBinding.selectImage.setImageURI(result);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    // upload content Method
    private void UploadLoanDataDialog(String loanId) {
        dialog = new Dialog(this);
        layoutBinding = LoanAppLayoutBinding.inflate(getLayoutInflater());
        dialog.setContentView(layoutBinding.getRoot());
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        // handle clicks
        layoutBinding.backBtnImg.setOnClickListener(view -> dialog.dismiss());

        // set data
        layoutBinding.titleTv.setText(loanId);
        layoutBinding.selectImage.setOnClickListener(v -> launcher.launch("image/*"));

        layoutBinding.okBtn.setOnClickListener(view -> {
            String title, intrest_rate, amount, s_age, reqment, s_url;

            title = layoutBinding.itemTitle.getText().toString().trim();
            intrest_rate = layoutBinding.interestRate.getText().toString().trim();
            amount = layoutBinding.amount.getText().toString().trim();
            s_age = layoutBinding.age.getText().toString().trim();
            reqment = layoutBinding.requirement.getText().toString().trim();
            s_url = layoutBinding.url.getText().toString().trim();

            if (encodedImage == null) {
                Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();

            } else if (TextUtils.isEmpty(title)) {
                layoutBinding.itemTitle.setError("Title required!");

            } else if (TextUtils.isEmpty(intrest_rate)) {
                layoutBinding.interestRate.setError("Field required!");

            } else if (TextUtils.isEmpty(amount)) {
                layoutBinding.amount.setError("Field required!");
            } else if (TextUtils.isEmpty(s_age)) {
                layoutBinding.age.setError("Field required!");
            } else if (TextUtils.isEmpty(reqment)) {
                layoutBinding.requirement.setError("Field required!");
            } else if (TextUtils.isEmpty(s_url)) {
                layoutBinding.url.setError("Field required!");
            } else {
                loadingDialog.show();
                map.put("img", encodedImage);
                map.put("title", title);
                map.put("interest", intrest_rate);
                map.put("amount", amount);
                map.put("age", s_age);
                map.put("req", reqment);
                map.put("url", s_url);
                map.put("loanId", loanId);
                uploadData(map);

            }

        });


    }

    private void uploadData(Map<String, String> map) {
        Call<MessageModel> call = apiInterface.uploadLoanAppData(map);
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadLoanAppActivity.this, Objects.requireNonNull(response.body()).getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(UploadLoanAppActivity.this, Objects.requireNonNull(response.body()).getError(), Toast.LENGTH_SHORT).show();

                }
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                Toast.makeText(UploadLoanAppActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
    // upload content Method


    private void ShowLoanAppData(String loanId) {
        loanAppDataLayoutDialog = new Dialog(this);
        loanAppDataLayoutBinding = EditLoanAppDataLayoutBinding.inflate(getLayoutInflater());
        loanAppDataLayoutDialog.setContentView(loanAppDataLayoutBinding.getRoot());
        loanAppDataLayoutDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        loanAppDataLayoutDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(UploadLoanAppActivity.this, R.drawable.ads_item_bg));
        loanAppDataLayoutDialog.setCancelable(false);
        loanAppDataLayoutDialog.show();
        loadingDialog.show();
        loanAppDataLayoutBinding.title.setText(loanId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        loanAppDataLayoutBinding.editRV.setLayoutManager(layoutManager);
        loanAppDataLayoutBinding.backBtn.setOnClickListener(view -> {
            loanAppDataLayoutDialog.dismiss();
            loanAppModels.clear();
        });

        loanAppsAdapter = new LoanAppsAdapter(this, this);
        loanAppDataLayoutBinding.editRV.setAdapter(loanAppsAdapter);
        fetchData(loanId);


//        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//
////                String itemId = loanAppModels.get(viewHolder.getAdapterPosition()).getId();
////                String imgPath = loanAppModels.get(viewHolder.getAdapterPosition()).getImg();
////                String loanIdS = loanAppModels.get(viewHolder.getAdapterPosition()).getLoanId();
////                loanAppModels.remove(viewHolder.getAdapterPosition());
////                loanAppsAdapter.updateLoanAppList(loanAppModels);
////
////                Log.d("contentValue", itemId + "  " + loanIdS + "  " + imgPath);
////
////                map.put("id", itemId);
////                map.put("loanId", loanIdS);
////                map.put("img", "loan_app_images/" + imgPath);
////                deleteItem(map);
//
//            }
//
//        };
//        new ItemTouchHelper(simpleCallback).attachToRecyclerView(loanAppDataLayoutBinding.editRV);

    }

    private void deleteItem(Map<String, String> map) {
        Call<MessageModel> call = apiInterface.deleteLoanAppData(map);
        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                assert response.body() != null;
                if (response.isSuccessful()) {
                    fetchData(map.get("loanId"));

                    Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), response.body().getError(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                Log.d("onResponse", t.getMessage());
            }
        });
    }

    private void fetchData(String loanId) {
        Call<LoanAppModelList> call = apiInterface.fetchLoanAppDetails(loanId);
        call.enqueue(new Callback<LoanAppModelList>() {
            @Override
            public void onResponse(@NonNull Call<LoanAppModelList> call, @NonNull Response<LoanAppModelList> response) {
                if (response.isSuccessful()) {
                    if (Objects.requireNonNull(response.body()).getData() != null) {
                        loanAppModels.clear();
                        loanAppModels.addAll(response.body().getData());
                        for (LoanAppModel loan : loanAppModels) {
                            Log.d("ContentValue", loan.getTitle());
                        }
                    } else {
                        Toast.makeText(UploadLoanAppActivity.this, "List is empty!", Toast.LENGTH_SHORT).show();
                    }
                    loanAppsAdapter.updateLoanAppList(loanAppModels);

                    loadingDialog.dismiss();

                }
            }

            @Override
            public void onFailure(@NonNull Call<LoanAppModelList> call, @NonNull Throwable t) {

            }
        });
//        return loanAppModelListMutableLiveData;

    }

    @Override
    public void onItemClicked(LoanAppModel loanAppModel, int position) {
        builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Edit your Item")
                .setMessage("Edit")
                .setNeutralButton("Delete", (dialog1, which) -> {
                    map.put("id", loanAppModel.getId());
                    map.put("loanId", loanAppModel.getLoanId());
                    map.put("img", "loan_app_images/" + loanAppModel.getImg());
                    deleteItem(map);

                });
        builder.setPositiveButton("Edit", (dialog, which) -> updateData(loanAppModel.getLoanId(), loanAppModel));
        builder.show();

    }

    private void updateData(String loanId, LoanAppModel loanAppModel) {

        dialog = new Dialog(this);
        layoutBinding = LoanAppLayoutBinding.inflate(getLayoutInflater());
        dialog.setContentView(layoutBinding.getRoot());
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        // handle clicks
        layoutBinding.backBtnImg.setOnClickListener(view -> dialog.dismiss());

        // set data
        layoutBinding.titleTv.setText(loanId);
        layoutBinding.selectImage.setOnClickListener(v -> launcher.launch("image/*"));

        layoutBinding.itemTitle.setText(loanAppModel.getTitle());
        layoutBinding.interestRate.setText(loanAppModel.getInterest());
        layoutBinding.amount.setText(loanAppModel.getAmount());
        layoutBinding.age.setText(loanAppModel.getAge());
        layoutBinding.requirement.setText(loanAppModel.getRequirement());
        layoutBinding.url.setText(loanAppModel.getUrl());
        Glide.with(this).load("https://gedgetsworld.in/Loan_App/loan_app_images/" + loanAppModel.getImg()).into(layoutBinding.selectImage);
        encodedImage = loanAppModel.getImg();


        layoutBinding.okBtn.setOnClickListener(view -> {
            String title, interest_rate, amount, s_age, reqment, s_url;

            title = layoutBinding.itemTitle.getText().toString().trim();
            interest_rate = layoutBinding.interestRate.getText().toString().trim();
            amount = layoutBinding.amount.getText().toString().trim();
            s_age = layoutBinding.age.getText().toString().trim();
            reqment = layoutBinding.requirement.getText().toString().trim();
            s_url = layoutBinding.url.getText().toString().trim();

            if (encodedImage == null) {
                Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(title)) {
                layoutBinding.itemTitle.setError("Title required!");
            } else if (TextUtils.isEmpty(interest_rate)) {
                layoutBinding.interestRate.setError("Field required!");
            } else if (TextUtils.isEmpty(amount)) {
                layoutBinding.amount.setError("Field required!");
            } else if (TextUtils.isEmpty(s_age)) {
                layoutBinding.age.setError("Field required!");
            } else if (TextUtils.isEmpty(reqment)) {
                layoutBinding.requirement.setError("Field required!");
            } else if (TextUtils.isEmpty(s_url)) {
                layoutBinding.url.setError("Field required!");
            } else {
                loadingDialog.show();

                if (encodedImage.length() <= 150) {

                    map.put("img", encodedImage);
                    map.put("deleteImg", loanAppModel.getImg());
                    map.put("title", title);
                    map.put("interest", interest_rate);
                    map.put("amount", amount);
                    map.put("age", s_age);
                    map.put("req", reqment);
                    map.put("url", s_url);
                    map.put("loanId", loanId);
                    map.put("id", loanAppModel.getId());
                    map.put("imgKey", "0");

                    uploadUpdateData(map);

                }

                if (encodedImage.length() > 150) {
                    map.put("img", encodedImage);
                    map.put("deleteImg", loanAppModel.getImg());
                    map.put("title", title);
                    map.put("interest", interest_rate);
                    map.put("amount", amount);
                    map.put("age", s_age);
                    map.put("req", reqment);
                    map.put("url", s_url);
                    map.put("loanId", loanId);
                    map.put("id", loanAppModel.getId());
                    map.put("imgKey", "1");
                    uploadUpdateData(map);
                }

                loadingDialog.show();

            }

        });

    }

    private void uploadUpdateData(Map<String, String> map) {
        Call<MessageModel> call = apiInterface.updateLoanAppsData(map);

        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(@NonNull Call<MessageModel> call, @NonNull Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadLoanAppActivity.this, Objects.requireNonNull(response.body()).getMessage(), Toast.LENGTH_SHORT).show();

                    fetchData(map.get("loanId"));
                }

                loadingDialog.dismiss();
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<MessageModel> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
            }
        });
    }
}