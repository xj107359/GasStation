package cn.sopho.destiny.gasstation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.gc.materialdesign.views.LayoutRipple;
import com.nineoldandroids.view.ViewHelper;

public class MainActivity extends Activity {
    LayoutRipple lrGoto1 = null;
    LayoutRipple lrGoto2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lrGoto1 = (LayoutRipple) findViewById(R.id.lr_goto1);
        setOriginRiple(lrGoto1);
        lrGoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Example1Activity.class);
                startActivity(intent);
            }
        });
        lrGoto2 = (LayoutRipple) findViewById(R.id.lr_goto2);
        setOriginRiple(lrGoto2);
        lrGoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Example2Activity.class);
                startActivity(intent);
            }
        });
    }

    private void setOriginRiple(final LayoutRipple layoutRipple) {
        layoutRipple.post(new Runnable() {

            @Override
            public void run() {
                View v = layoutRipple.getChildAt(0);
                layoutRipple.setxRippleOrigin(ViewHelper.getX(v) + v.getWidth() / 2);
                layoutRipple.setyRippleOrigin(ViewHelper.getY(v) + v.getHeight() / 2);
                layoutRipple.setRippleColor(Color.parseColor("#1E88E5"));
                layoutRipple.setRippleSpeed(30);
            }
        });
    }
}
