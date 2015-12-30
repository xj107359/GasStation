package cn.sopho.destiny.gasstation;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailActivity extends android.app.Dialog {
    private Context mContext = null;
    private Integer mImgSrc = 0;
    private String mTitle = null, mAddr = null;
    private boolean mShowAddr = false;
    private RelativeLayout mRlytBack = null;
    private LinearLayout mLlytContent = null;
    private ImageView mIvPopupImage = null;
    private TextView mTvPopupTitle = null;
    private TextView mTvPopupAddr = null;
    private int mContentPadTop = 65;

    public DetailActivity( ){
        super(null);
    }

    public DetailActivity(Context context, Integer imgSrc, String title) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mImgSrc = imgSrc;
        this.mTitle = title;
        mShowAddr = false;
        mContentPadTop = 65;
    }

    public DetailActivity(Context context, Integer imgSrc, String title, String addr) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mImgSrc = imgSrc;
        this.mTitle = title;
        this.mAddr = addr;
        mShowAddr = true;
        mContentPadTop = 85;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mIvPopupImage = (ImageView) findViewById(R.id.iv_popup_image);
        mIvPopupImage.setImageResource(mImgSrc);
        mTvPopupTitle = (TextView) findViewById(R.id.tv_popup_title);
        mTvPopupTitle.setText(mTitle);
        mTvPopupAddr = (TextView) findViewById(R.id.tv_popup_addr);
        if (mShowAddr) {
            mTvPopupAddr.setText(mAddr);
        } else {
            mTvPopupAddr.setText("");
        }

        mLlytContent = (LinearLayout) findViewById(R.id.llyt_content);
        mLlytContent.setPadding(0, mContentPadTop, 0, 0);// left, top, right, bottom
        mRlytBack = (RelativeLayout) findViewById(R.id.rlyt_back);
        mRlytBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getX() < mLlytContent.getLeft() || event.getX() > mLlytContent.getRight()
                        || event.getY() > mLlytContent.getBottom() || event.getY() < mLlytContent.getTop()) {
                    dismiss();
                }
                return false;
            }
        });
    }

    @Override
    public void show() {
        super.show();
        mLlytContent.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.dialog_main_show_amination));
        mRlytBack.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.dialog_root_show_amin));
    }

    @Override
    public void dismiss() {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.dialog_main_hide_amination);

        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLlytContent.post(new Runnable() {
                    @Override
                    public void run() {
                        DetailActivity.super.dismiss();
                    }
                });
            }
        });

        Animation backAnim = AnimationUtils.loadAnimation(mContext, R.anim.dialog_root_hide_amin);

        mLlytContent.startAnimation(anim);
        mRlytBack.startAnimation(backAnim);
    }
}
