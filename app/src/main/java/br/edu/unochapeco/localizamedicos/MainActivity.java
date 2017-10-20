package br.edu.unochapeco.localizamedicos;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity implements DownloadResultReceiver.Receiver {

    private DownloadResultReceiver mReceiver;
    private String especiality = "http://mobile-aceite.tcu.gov.br:80/mapa-da-saude/rest/especialidades/unidade/4204202537788";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getEspeviality();
    }

    private void getEspeviality(){
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

        intent.putExtra("type", "espevility");
        intent.putExtra("url", especiality);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        startService(intent);
    }

    public void callListDoctors(View view){
        Spinner spTipo = (Spinner) findViewById(R.id.spinner);
        Intent i = new Intent(this, GridActivity.class);
        i.putExtra("especialityFilter", spTipo.getSelectedItem().toString());
        startActivity(i);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case DownloadService.STATUS_RUNNING:
                Log.d("Trabalhando ainda","Trababalhando ainda....");
                break;
            case DownloadService.STATUS_FINISHED:
                this.setEspeviality(resultData);
                break;
            case DownloadService.STATUS_ERROR:
                Log.d("Erro", "Erro");
                break;
        }
    }

    public void setEspeviality(Bundle resultData){
        String[] results = resultData.getStringArray("result");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);
        Spinner spTipo = (Spinner) findViewById(R.id.spinner);
        spTipo.setAdapter(arrayAdapter);
    }

}
