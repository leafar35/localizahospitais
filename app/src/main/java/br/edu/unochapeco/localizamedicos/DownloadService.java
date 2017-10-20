package br.edu.unochapeco.localizamedicos;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DownloadService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private static final String TAG = "DownloadService";

    //Construtor
    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Iniciado!");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");
        String type = intent.getStringExtra("type");
        String criteria = intent.getStringExtra("pharse");

        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(url)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            try {
                String[] results = downloadData(url,type, criteria);
                if (null != results && results.length > 0) {
                    bundle.putString("type", type);
                    bundle.putStringArray("result", results);
                    receiver.send(STATUS_FINISHED, bundle);
                }
            } catch (Exception e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }
        Log.d(TAG, "Service parado!");
        this.stopSelf();
    }

    private String[] downloadData(String requestUrl, String type, String criteria) throws IOException, DownloadException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        URL url = new URL(requestUrl);

        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            String[] results = null;
            if(type.equals("espevility")){
                results = parseResult(response);
            }else if(type.equals("establishment")){
                results = parseResultEstablish(response, criteria);
            }else {
                results = parseResultAddress(response);
            }
            return results;
        } else {
            throw new DownloadException("Falha ao buscar dados!!");
        }
    }

    public String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        if (null != inputStream) {
            inputStream.close();
        }
        return result;
    }

    private String[] parseResult(String result) {
        String[] specialties = null;
        try {
            JSONArray response = new JSONArray(result);
            specialties = new String[response.length()];
            for (int i = 0; i < response.length(); i++) {
                JSONObject Specialties = response.optJSONObject(i);
                String title = Specialties.optString("descricaoHabilitacao");
                specialties[i] = title;
            }
            Set<String> set = new HashSet<String>(Arrays.asList(specialties));
            return set.toArray(new String[set.size()]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return specialties;
    }

    private String[] parseResultEstablish(String result, String criteria) {
        int indice = 0;
        String[] establish = null;
        try {
            JSONArray response = new JSONArray(result);
            establish = new String[response.length()];
            for (int i = 0; i < response.length(); i++) {
                JSONObject Specialties = response.optJSONObject(i);
                //Log.d("++++++++++++++",""+Specialties.optString("descricaoCompleta").indexOf(criteria) != -1);
                //boolean isTrue = (Specialties.optString("descricaoCompleta").indexOf(criteria) != -1);
                establish[i] = "[{'nomeFantasia':" + Specialties.optString("nomeFantasia").toString().replace(" ", "") + "},{'long':" + Specialties.optString("long") + "},{'lat':" + Specialties.optString("lat") + "},{'codUnidade':" + Specialties.optString("codUnidade") + "}]";
            }
            return establish;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return establish;
    }

    private String[] parseResultAddress(String result){
        String[] address = null;
        try {
            JSONArray response = new JSONArray(result);
            address = new String[response.length()];
            for (int i = 0; i < response.length(); i++) {
                JSONObject establishment = response.optJSONObject(i);
                address[i] = "[{'nomeFantasia':"+establishment.optString("nomeFantasia").replace(" ", "")+"},{'bairro':"+establishment.optString("bairro")+"},{'cidade':"+establishment.optString("cidade")+"},{'numero':"+establishment.optString("numero")+"},{'logradouro':"+establishment.optString("logradouro").replace(" ", "")+"},{'telefone':"+establishment.optString("telefone")+"}]";
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return address;
    }

    public class DownloadException extends Exception {
        public DownloadException(String message) {
            super(message);
        }
        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
