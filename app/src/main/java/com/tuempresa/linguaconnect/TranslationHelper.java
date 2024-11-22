package com.tuempresa.linguaconnect;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class TranslationHelper {

    private static final String TAG = "TranslationHelper";
    private static final String API_KEY = "AIzaSyAuMDPZfK7bIDyTjUXCPL__vC2RLOJ69b0"; // **¡Protege esta clave!**

    public static void translateText(String text, String targetLanguage, TranslationCallback callback, Context context) {
        OkHttpClient client = new OkHttpClient();

        try {
            String url = "https://translation.googleapis.com/language/translate/v2?q="
                    + URLEncoder.encode(text, "UTF-8")
                    + "&target=" + targetLanguage
                    + "&key=" + API_KEY;

            Log.d(TAG, "Solicitud de traducción: " + url);

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error en la solicitud de traducción", e);
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Respuesta de la API: " + responseBody);
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String translatedText = jsonObject
                                    .getJSONObject("data")
                                    .getJSONArray("translations")
                                    .getJSONObject(0)
                                    .getString("translatedText");

                            if (translatedText != null && !translatedText.isEmpty()) {
                                callback.onSuccess(translatedText);
                            } else {
                                Log.e(TAG, "La traducción está vacía.");
                                callback.onFailure(new Exception("La traducción está vacía."));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear la respuesta JSON", e);
                            callback.onFailure(e);
                        }
                    } else {
                        Log.e(TAG, "Error en la respuesta de la API: " + response.code() + " - " + responseBody);
                        try {
                            JSONObject errorObject = new JSONObject(responseBody);
                            String errorMessage = errorObject.getJSONObject("error").getString("message");
                            callback.onFailure(new IOException("Error de la API: " + errorMessage));
                        } catch (JSONException e) {
                            callback.onFailure(new IOException("Error desconocido en la respuesta de la API"));
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al construir la solicitud de traducción", e);
            callback.onFailure(e);
        }
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onFailure(Exception e);
    }
}
