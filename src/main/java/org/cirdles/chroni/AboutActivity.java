/**
 * This activity displays the about screen of the application which can be reached through the menu.
 */

package org.cirdles.chroni;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Sets up activity screen
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.about);

        // Makes About text scrollable
        TextView aboutText = (TextView) findViewById(R.id.aboutText);
        aboutText.setMovementMethod(new ScrollingMovementMethod());

        // Programs button for main screen
        Button aboutHomeButton = (Button) findViewById(R.id.fileBrowserHomeButton);
        aboutHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.table_menu, menu);
        return true;
    }

    /**
     * The purpose of overriding this method is to alter/delete some of the menu items from the default
     * menu, as they are not wanted in this Activity. Doing so prevents the unnecessary stacking of
     * Activities by making the user follow the intended flow of Activities in the application.
     *
     * @param menu the menu that has been inflated in the Activity
     * @return true so that the menu is displayed
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // changes the Exit item to say "Back" instead
        MenuItem exitItem = menu.findItem(R.id.exitMenu);
        exitItem.setTitle("Back");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles menu item selection
        switch (item.getItemId()) {
            case R.id.exitMenu:
                finish();
                return true;

            case R.id.helpMenu: // Takes user to help blog
                // Checks internet connection before downloading files
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobileWifi.isConnected()) {
                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.chroni_help_address)));
                    startActivity(openHelpBlog);

                } else {
                    new AlertDialog.Builder(this).setMessage("You are not connected to WiFi, mobile data rates may apply. " +
                            "Do you wish to continue?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent openHelpBlog = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.chroni_help_address)));
                                    startActivity(openHelpBlog);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}