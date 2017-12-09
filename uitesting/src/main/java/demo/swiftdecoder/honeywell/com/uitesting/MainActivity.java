package demo.swiftdecoder.honeywell.com.uitesting;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.honeywell.lib.widgets.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_viewpager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SampleActivity.class));
            }
        });
    CircleWaveView loading = (CircleWaveView) findViewById(R.id.loading);
        loading.setWaveColor(Color.parseColor("#2D9CE8"));
        loading.setWaveInterval(50);
    }
}
