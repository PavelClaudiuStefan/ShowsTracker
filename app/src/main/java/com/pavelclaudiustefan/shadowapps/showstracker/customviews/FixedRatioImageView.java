package com.pavelclaudiustefan.shadowapps.showstracker.customviews;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class FixedRatioImageView extends androidx.appcompat.widget.AppCompatImageView {

    public FixedRatioImageView(Context context) {
        super(context);
    }

    public FixedRatioImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRatioImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();

        //force a 16:9 aspect ratio
        int height = Math.round(width * .5625f);
        setMeasuredDimension(width, height);
    }

}
