package cz.iqlandia.iqplanetarium.scanner.knowledge;

import static cz.iqlandia.iqplanetarium.scanner.disambiguation.MainActivity.messages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Random;

import cz.iqlandia.iqplanetarium.scanner.R;

public class KnowledgeBook extends AppCompatActivity {
    private KnowledgeBook activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_knowledge_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.kb_tree_kosmo).setOnClickListener(c-> {
            Intent i = new Intent(activity, KBKosmoActivity.class);
            c.getContext().startActivity(i);
        });
        List<Integer> disabled = List.of(R.id.kb_tree_terasa,
                R.id.kb_tree_veda_doma,
                R.id.kb_tree_smysly,
                R.id.kb_tree_clovek,
                R.id.kb_tree_matematikum,
                R.id.kb_tree_tulka,
                R.id.kb_tree_geo,
                R.id.kb_tree_zivly,
                R.id.kb_tree_voda,
                R.id.kb_tree_vynalezy,
                R.id.kb_tree_geolab);

        for (Integer i : disabled) {
            findViewById(i).setOnClickListener(c-> runOnUiThread(()->{
                Toast.makeText(this, getString(messages[new Random().nextInt(messages.length)]), Toast.LENGTH_SHORT).show();
            }));
            findViewById(i).setClickable(false);
        }
    }
}