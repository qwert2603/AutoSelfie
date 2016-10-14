package com.qwert2603.autoselfie.helpers;

import android.graphics.Bitmap;

import com.qwert2603.autoselfie.utils.LogUtils;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VkHelper {

    public static void logout() {
        VKSdk.logout();
    }

    public interface Callback {
        void onSuccess();

        void onError(Throwable throwable);
    }

    private static DateFormat sDateFormat = new SimpleDateFormat("'AutoSelfie' dd.MM.yyyy '@' HH:mm:ss", Locale.getDefault());

    public void sendPhoto(Bitmap bitmap, long photoTime, Callback callback) {
        VKRequest vkRequest = VKApi.uploadMessagesPhotoRequest(new VKUploadImage(bitmap, VKImageParameters.pngImage()));
        vkRequest.setUseLooperForCallListener(false);
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                LogUtils.d("response.json.toString() == " + response.json.toString());
                try {
                    JSONArray jsonArray = response.json.getJSONArray("response");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    int id = jsonObject.getInt("id");
                    int owner_id = jsonObject.getInt("owner_id");
                    LogUtils.d("id == " + id + ";owner_id == " + owner_id);
                    VKParameters vkParameters = VKParameters.from(
                            VKApiConst.USER_ID, owner_id,
                            VKApiConst.MESSAGE, sDateFormat.format(new Date(photoTime)),
                            "attachment", "photo" + owner_id + "_" + id);
                    VKRequest vkRequest1 = new VKRequest("messages.send", vkParameters);
                    vkRequest1.setUseLooperForCallListener(false);
                    vkRequest1.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            LogUtils.d("sendPhoto#onComplete");
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(VKError error) {
                            callback.onError(new RuntimeException(error.toString()));
                        }
                    });
                } catch (JSONException e) {
                    callback.onError(new RuntimeException(e.toString()));
                }
            }

            @Override
            public void onError(VKError error) {
                callback.onError(new RuntimeException(error.toString()));
            }
        });
    }

}
