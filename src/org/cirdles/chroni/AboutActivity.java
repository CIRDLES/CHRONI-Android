package org.cirdles.chroni;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class AboutActivity extends Activity  {

    // Version number
    private TextView versionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

            Context context = this;
//            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
//            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
//
//            versionNumber = (TextView) findViewById(R.id.versionNumber);
//            versionNumber.setText("Version " + versionCode + "." + versionName);
//            versionNumber.setTextColor(getResources().getColor(
//                    R.color.button_blue));

//        Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
//        startActivity(openMainMenu);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.returnToMenu:
                Intent openMainMenu = new Intent("android.intent.action.MAINMENU");
                startActivity(openMainMenu);
                return true;
            case R.id.editProfileMenu:
                Intent openUserProfile = new Intent(
                        "android.intent.action.USERPROFILE");
                startActivity(openUserProfile);
                return true;
            case R.id.aboutScreen:
                Intent openAboutScreen = new Intent(
                        "android.intent.action.ABOUT");
                startActivity(openAboutScreen);
                return true;
            case R.id.helpMenu:
                Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://chronihelpblog.wordpress.com"));
                startActivity(openHelpBlog);
                return true;
            case R.id.exitProgram:
                finish();
                System.exit(0);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}