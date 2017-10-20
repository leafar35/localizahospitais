package br.edu.unochapeco.localizamedicos;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends Activity implements DownloadResultReceiver.Receiver {

    //private GoogleMap mMap;
    private DownloadResultReceiver mReceiver;
    private String urlEstablishments = "http://mobile-aceite.tcu.gov.br:80/mapa-da-saude/rest/estabelecimentos/unidade/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        this.getEstablishment();
    }

    private void getEstablishment(){
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);
        String unidade = this.getIntent().getStringExtra("especialityFilter");

        intent.putExtra("type", "detail");
        intent.putExtra("url", this.urlEstablishments+unidade);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        startService(intent);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case DownloadService.STATUS_RUNNING:
                Log.d("Trabalhando ainda","Trababalhando ainda....");
                break;
            case DownloadService.STATUS_FINISHED:
                Log.d("Dtail","balalaiksaika");
                this.setLocalization(resultData);
                break;
            case DownloadService.STATUS_ERROR:
                Log.d("Erro", "Erro");
                break;
        }
    }

    private void setLocalization(Bundle resultData) {
        String[] results = resultData.getStringArray("result");
        String value = results[0].toString();
        try {
            JSONArray response = new JSONArray(value.toString());
            JSONObject nomeJson = new JSONObject(response.get(0).toString());
            JSONObject bairroJson = new JSONObject(response.get(1).toString());
            JSONObject cidadeJson = new JSONObject(response.get(2).toString());
            JSONObject numeroJson = new JSONObject(response.get(3).toString());
            JSONObject telefojson = new JSONObject(response.get(5).toString());

            TextView textName = (TextView) findViewById(R.id.textView3);
            TextView cidade = (TextView) findViewById(R.id.textView4);
            TextView endereco = (TextView) findViewById(R.id.textView5);
            TextView telefone = (TextView) findViewById(R.id.textView6);

            telefone.setText(" - "+telefojson.get("telefone").toString());
            textName.setText("Nome Fantasia.: "+nomeJson.get("nomeFantasia").toString());
            cidade.setText("Cidade.: "+cidadeJson.get("cidade").toString());
            endereco.setText("Endereço .: "+bairroJson.get("bairro").toString()+" Numero.: "+numeroJson.get("numero").toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void mapa(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void callListDoctors(View view){
        Intent i = new Intent(this, GridActivity.class);
        startActivity(i);
    }

}