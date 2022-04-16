package com.instantloanguide.loanguideadmin.services;

import com.instantloanguide.loanguideadmin.models.AdsModelList;
import com.instantloanguide.loanguideadmin.models.MessageModel;
import com.instantloanguide.loanguideadmin.models.NewsModelList;
import com.instantloanguide.loanguideadmin.models.TipsModelList;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

//    @POST("fetch_kisan_yojana_api.php")
//    Call<YojanaModelList> getAllYojana();
//
//    @POST("fetch_others_api.php")
//    Call<YojanaModelList> getAllOthers();
//

    //    @FormUrlEncoded
//    @POST("upload_kisan_yojna_api.php")
//    Call<MessageModel> uploadYojana(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("upload_others_api.php")
//    Call<MessageModel> uploadOthers(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("upload_news_api.php")
//    Call<MessageModel> uploadNews(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("upload_preview_data.php")
//    Call<MessageModel> uploadPreviewData(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("fetch_preview_data.php")
//    Call<PreviewModelList> getPreview(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("update_news_api.php")
//    Call<MessageModel> updateNews(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("Delete_api.php")
//    Call<MessageModel> deleteItems(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("update_yojana_api.php")
//    Call<MessageModel> updateYojana(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("update_others_api.php")
//    Call<MessageModel> updateOthers(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("update_preview_api.php")
//    Call<MessageModel> updatePreview(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("upload_pin_status.php")
//    Call<MessageModel> uploadPinStatus(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("upload_quiz_questions.php")
//    Call<MessageModel> uploadQuizQuestions(@FieldMap Map<String, String> map);
//
//    @POST("fetch_quiz_questions.php")
//    Call<QuizModelList> fetchQuizQuestions();
//
//    @FormUrlEncoded
//    @POST("Delete_quiz_api.php")
//    Call<MessageModel> deleteQuizItems(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("update_quiz_api.php")
//    Call<MessageModel> updateQuiz(@FieldMap Map<String, String> map);
//
//
//    @FormUrlEncoded
//    @POST("pm_kisan_ads_id_fetch.php")
//    Call<PMAdsModelList> fetchAdsPm(@Field("id") String id);
//
//    @FormUrlEncoded
//    @POST("pm_kisan_id_update.php")
//    Call<MessageModel> updatePMAdIds(@FieldMap Map<String, String> map);
//
//    @FormUrlEncoded
//    @POST("ads_id_fetch.php")
//    Call<AdsModelList> fetchAds(@Field("id") String id);
//
//    @FormUrlEncoded
//    @POST("ads_id_update.php")
//    Call<MessageModel> updateAdIds(@FieldMap Map<String, String> map);
//
//    @POST("fetchImg.php")
//    Call<ImgModel> fetchImg();
//
    @FormUrlEncoded
    @POST("upload_news_api.php")
    Call<MessageModel> uploadNews(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("upload_tips_api.php")
    Call<MessageModel> uploadTips(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("upload_strip_ban_api.php")
    Call<MessageModel> uploadStripBan(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("update_news_api.php")
    Call<MessageModel> updateNews(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("update_tips_api.php")
    Call<MessageModel> updateTips(@FieldMap Map<String, String> map);

    @POST("fetch_news_api.php")
    Call<NewsModelList> getAllNews();

    @POST("fetch_tips_api.php")
    Call<TipsModelList> getAllTips();

    @FormUrlEncoded
    @POST("delete_news.php")
    Call<MessageModel> deleteNews(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("delete_tips.php")
    Call<MessageModel> deleteTips(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("ads_id_fetch.php")
    Call<AdsModelList> fetchAds(@Field("id") String id);

    @FormUrlEncoded
    @POST("ads_id_update.php")
    Call<MessageModel> updateAdIds(@FieldMap Map<String, String> map);
}
