package com.rizkytm.myqrcode;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rizkytm.myqrcode.model.QRGeoModel;
import com.rizkytm.myqrcode.model.QRURLModel;
import com.rizkytm.myqrcode.model.QRVCardModel;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private TextView txtProgram;
    private TextView txtNama;
    private TextView txtNoKonfirmasi;
    private TextView txtJenisTiket;
    private TextView txtKeterangan;
    private String program;
    private String nama;
    private String jenisTiket;
    private String status;
//    private Button btnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannerView = (ZXingScannerView) findViewById(R.id.zxscan);
        txtProgram = (TextView) findViewById(R.id.txt_program);
        txtNama = (TextView) findViewById(R.id.txt_nama);
        txtNoKonfirmasi = (TextView) findViewById(R.id.txt_no_konfirmasi);
        txtJenisTiket = (TextView) findViewById(R.id.txt_jenis_tiket);
        txtKeterangan = (TextView) findViewById(R.id.txt_keterangan);

//        btnRestart = (Button) findViewById(R.id.restart);
//        btnRestart.setText("Send Data");
//        btnRestart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
//                try {
//                    sendData();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                txtProgram.setText("Program : ");
//                txtNama.setText("Nama : ");
//                txtNoKonfirmasi.setText("No Konfirmasi : ");
//                txtJenisTiket.setText("Jenis Tiket : ");
//                txtKeterangan.setText("Status");
//                scannerView.startCamera();
//            }
//        });

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    public void sendData(final String noKonfirmasi) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL("http://merryriana.com/server_api/absensi");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject json = new JSONObject();
                    json.put("no_konfirmasi", noKonfirmasi);

                    Log.i("JSON", json.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(json.toString());

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());


                    status = jsonResponse.getString("keterangan");
                    status = status.toUpperCase();
                    if (status.equals("SUCCESS") || status.equals("ATTENDED")) {
                        nama = jsonResponse.getString("nama");
                        program = jsonResponse.getString("program");
                        jenisTiket = jsonResponse.getString("jenis_tiket");

                        txtProgram.setText("Program : " + program);
                        txtNama.setText("Nama : " + nama);
                        txtNoKonfirmasi.setText("No Konfirmasi : " + noKonfirmasi);
                        txtJenisTiket.setText("Jenis Tiket : " + jenisTiket);
                        txtKeterangan.setText(status);
                        txtKeterangan.setTextColor(Color.GREEN);
                    } else {
                        nama = null;
                        program = null;
                        jenisTiket = null;

                        txtProgram.setText("Program : " + program);
                        txtNama.setText("Nama : " + nama);
                        txtNoKonfirmasi.setText("No Konfirmasi : " + noKonfirmasi);
                        txtJenisTiket.setText("Jenis Tiket : " + jenisTiket);
                        txtKeterangan.setText(status);
                        txtKeterangan.setTextColor(Color.RED);
                    }

//                    txtProgram.setText("Program : " + program);
//                    txtNama.setText("Nama : " + nama);
//                    txtNoKonfirmasi.setText("No Konfirmasi : " + noKonfirmasi);
//                    txtJenisTiket.setText("Jenis Tiket : " + jenisTiket);
//                    txtKeterangan.setText(status);

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    conn.disconnect();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        processRawResult(rawResult.getText());
        scannerView.startCamera();
    }

    private void processRawResult(String text) {
//        if (text.startsWith("BEGIN:")) {
//            String[] tokens = text.split("\n");
//            QRVCardModel qrvCardModel = new QRVCardModel();
//            for (int i = 0; i < tokens.length; i++) {
//                if (tokens[i].startsWith("BEGIN:")) {
//                    qrvCardModel.setType(tokens[i].substring("BEGIN:".length()));
//                } else if (tokens[i].startsWith("N:")) {
//                    qrvCardModel.setName(tokens[i].substring("N:".length()));
//                } else if (tokens[i].startsWith("ORG:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("ORG:".length()));
//                } else if (tokens[i].startsWith("TEL:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("TEL:".length()));
//                } else if (tokens[i].startsWith("URL:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("URL:".length()));
//                } else if (tokens[i].startsWith("EMAIL:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("EMAIL:".length()));
//                } else if (tokens[i].startsWith("ADR:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("ADR:".length()));
//                } else if (tokens[i].startsWith("NOTE:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("NOTE:".length()));
//                } else if (tokens[i].startsWith("SUMMARY:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("SUMMARY:".length()));
//                } else if (tokens[i].startsWith("DTSTART:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("DTSTART:".length()));
//                } else if (tokens[i].startsWith("DTEND:")) {
//                    qrvCardModel.setOrg(tokens[i].substring("DTEND:".length()));
//                }
//
//                txtProgram.setText(qrvCardModel.getType());
//                Toast.makeText(this, "Berhasil " + text, Toast.LENGTH_SHORT).show();
//
//            }
//
//        } else if (text.startsWith("http://") ||
//                text.startsWith("https://") ||
//                text.startsWith("www.")) {
//            QRURLModel qrurlModel = new QRURLModel(text);
//            txtProgram.setText(qrurlModel.getUrl());
//            Toast.makeText(this, "Berhasil " + text, Toast.LENGTH_SHORT).show();
//
//        } else if (text.startsWith("geo:")) {
//            QRGeoModel qrGeoModel = new QRGeoModel();
//            String delims = "[ , ?q= ]+";
//            String tokens[] = text.split(delims);
//
//            for (int i = 0; i < tokens.length; i++) {
//                if (tokens[i].startsWith(" geo:")) {
//                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
//                }
//            }
//            qrGeoModel.setLat(tokens[0].substring("geo".length()));
//            qrGeoModel.setLng(tokens[1]);
//            qrGeoModel.getGeo_place(tokens[2]);
//
//            txtProgram.setText(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());
//            Toast.makeText(this, "Berhasil " + text, Toast.LENGTH_SHORT).show();
//        } else {
//            txtProgram.setText(text);
//            Toast.makeText(this, "Berhasil " + text, Toast.LENGTH_SHORT).show();
//        }
        try {
            sendData(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scannerView.resumeCameraPreview(MainActivity.this);
    }
}
