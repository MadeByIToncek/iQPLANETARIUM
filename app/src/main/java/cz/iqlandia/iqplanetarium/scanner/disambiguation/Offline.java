/*
 * All rights reserved to IToncek
 */

/*
 * All rights reserved to IToncek
 */

package cz.iqlandia.iqplanetarium.scanner.disambiguation;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import cz.iqlandia.iqplanetarium.scanner.R;

public class Offline extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_offline);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }
}