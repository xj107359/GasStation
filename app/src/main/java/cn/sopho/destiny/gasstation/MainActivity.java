package cn.sopho.destiny.gasstation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gc.materialdesign.views.LayoutRipple;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LayoutRipple lrGoto1 = (LayoutRipple) findViewById(R.id.lr_goto1);
        LayoutRipple lrGoto2 = (LayoutRipple) findViewById(R.id.lr_goto2);

        lrGoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Example1Activity.class);
                startActivity(intent);
            }
        });
        lrGoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Example2Activity.class);
                startActivity(intent);
            }
        });
    }
}
