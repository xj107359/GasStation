package cn.sopho.destiny.gasstation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGoto1 = (Button)findViewById(R.id.goto1_btn);
        Button btnGoto2 = (Button)findViewById(R.id.goto2_btn);

        btnGoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Example1Activity.class);
                startActivity(intent);
            }
        });
        btnGoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Example2Activity.class);
                startActivity(intent);
            }
        });
    }
}
