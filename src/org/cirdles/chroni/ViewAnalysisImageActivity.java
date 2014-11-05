/*
Able to get ths code from http://www.learn2crack.com/2014/06/android-load-image-from-internet.html
 */

package org.cirdles.chroni;
        import java.io.InputStream;
        import java.net.URL;

        import android.content.pm.ActivityInfo;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

public class ViewAnalysisImageActivity extends Activity {
    Button saveAnalysisImage;
    ImageView viewAnalysisImage;
    TextView analysisImageText;
    Bitmap bitmap;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_analysis_image);
        setTheme(android.R.style.Theme_Holo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        viewAnalysisImage = (ImageView)findViewById(R.id.viewAnalysisImage);
        analysisImageText = (TextView) findViewById(R.id.analysisImageText);
        new LoadImage().execute("http://www.geochronportal.org/uploadimages/16747-concordia-tempConcordia.svg");


        if(getIntent().getStringExtra("ProbabilityDensityImage") != null){
            analysisImageText.setText("Probability Density");
        }

        if(getIntent().getStringExtra("ConcordiaImage") != null){
            analysisImageText.setText("Concordia");
//            new LoadImage().execute(getIntent().getStringExtra("ConcordiaImage"));

        }

        saveAnalysisImage = (Button)findViewById(R.id.saveAnalysisImageButton);
        saveAnalysisImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }
    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ViewAnalysisImageActivity.this);
            progressDialog.setMessage("Loading Image ....");
            progressDialog.show();
        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap image) {
            if(image != null){
                viewAnalysisImage.setImageBitmap(image);
                progressDialog.dismiss();
            }else{
                progressDialog.dismiss();
                Toast.makeText(ViewAnalysisImageActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}