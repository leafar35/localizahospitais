package br.edu.unochapeco.localizamedicos;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GridActivity extends Activity implements DownloadResultReceiver.Receiver {

    ListView listItemView;
    public String[] listUnidades = null;
    private DownloadResultReceiver mReceiver;
    private String establishments = "http://mobile-aceite.tcu.gov.br/mapa-da-saude/rest/estabelecimentos/latitude/$latitude/longitude/$longitude/raio/10?categoria=hospital&especialidade=$especialidade?quantidade=3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        GPSTracker gpsTracker = new GPSTracker(this);

        if(this.listUnidades != null ){
            listUnidades = new String[this.listUnidades.length];
        }

        Log.d("Grid",this.getIntent().getStringExtra("especialityFilter"));
        this.getEstablishments(gpsTracker);

        listItemView = (ListView)findViewById(R.id.listDistanci);
        listItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent two = new Intent(getApplicationContext(), DetailActivity.class);
                two.putExtra("especialityFilter", listUnidades[i]);
                startActivity(two);
            }
        });
    }

    private void getEstablishments(GPSTracker gpsTracker){
        establishments = establishments
                .replace("$latitude", ""+gpsTracker.getLongitude())
                .replace("$longitude", ""+gpsTracker.getLatitude())
                .replace("$especialidade",this.getIntent().getStringExtra("especialityFilter").toString());

        Log.d("Grid", establishments);
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

        intent.putExtra("pharse", this.getIntent().getStringExtra("especialityFilter"));
        intent.putExtra("type", "establishment");
        intent.putExtra("url", establishments);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        startService(intent);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        GPSTracker gpsTracker = new GPSTracker(this);
        switch (resultCode) {
            case DownloadService.STATUS_RUNNING:
                Log.d("Trabalhando ainda","Trababalhando ainda....");
                break;
            case DownloadService.STATUS_FINISHED:
                String[] results = resultData.getStringArray("result");
                String[] sendView = new String[results.length];
                this.listUnidades = new String[results.length];
                for (int i = 0; i < results.length; i++) {
                    String value = results[i].toString();
                    try {
                        JSONArray response = new JSONArray(value.toString());
                        JSONObject nomeFantasia = new JSONObject(response.get(0).toString());
                        JSONObject lon = new JSONObject(response.get(1).toString());
                        JSONObject lat = new JSONObject(response.get(2).toString());
                        JSONObject unidade = new JSONObject(response.get(3).toString());
                        double latD = ((double) lat.get("lat"));
                        double lonD = ((double)lon.get("long"));
                        double dist = this.distance(latD, lonD, gpsTracker.latitude, gpsTracker.longitude, "N");
                        this.listUnidades[i] = unidade.get("codUnidade").toString();
                        sendView[i] = nomeFantasia.get("nomeFantasia").toString() + " \n " + String.format("Distancia: %.2f", dist);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                this.setListProfessionalUpcoming(sendView);
                break;
            case DownloadService.STATUS_ERROR:
                Log.d("Erro", "Erro");
                break;
        }
    }

    private void setListProfessionalUpcoming(String[] results){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);
        ListView spTipo = (ListView) findViewById(R.id.listDistanci);
        spTipo.setAdapter(arrayAdapter);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public void backFindEspeciality(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
