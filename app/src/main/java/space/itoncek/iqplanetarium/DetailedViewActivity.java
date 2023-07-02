/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DetailedViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        String id = this.getIntent().getStringExtra("id");
        //((TextView) findViewById(R.id.te)).setText(id);
    }
}