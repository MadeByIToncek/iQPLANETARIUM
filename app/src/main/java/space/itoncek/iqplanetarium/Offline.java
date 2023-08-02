/*
 * All rights reserved to IToncek
 */

/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Offline extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }
}