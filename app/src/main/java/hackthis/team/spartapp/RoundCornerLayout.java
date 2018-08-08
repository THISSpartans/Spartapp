package hackthis.team.spartapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class RoundCornerLayout extends AppCompatImageView {

    //取自https://blog.csdn.net/angcyo/article/details/51171299

        private float roundLayoutRadius = 13f;
        private Path roundPath;
        private RectF rectF;

        public RoundCornerLayout(Context context) {
            super(context, null);
        }

        public RoundCornerLayout(Context context, AttributeSet attrs){
            super(context, attrs);
            roundLayoutRadius = 13;
            init();
        }

        private void init() {
            setWillNotDraw(false);//如果你继承的是ViewGroup,注意此行,否则draw方法是不会回调的;
            roundPath = new Path();
            rectF = new RectF();
        }

        private void setRoundPath() {
            //添加一个圆角矩形到path中, 如果要实现任意形状的View, 只需要手动添加path就行
            roundPath.addRoundRect(rectF, roundLayoutRadius, roundLayoutRadius, Path.Direction.CW);
        }


        public void setRoundLayoutRadius(float roundLayoutRadius) {
            this.roundLayoutRadius = roundLayoutRadius;
            setRoundPath();
            postInvalidate();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            rectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
            setRoundPath();
        }

        @Override
        public void draw(Canvas canvas) {
            if (roundLayoutRadius > 0f) {
                canvas.clipPath(roundPath);
            }
            super.draw(canvas);
        }

}
