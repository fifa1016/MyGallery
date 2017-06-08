package com.wang.mygallery.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by wang on 16-9-6.
 */

public class MyImageView extends ImageView implements View.OnTouchListener {
    private static final String TAG = "MyImageView";

    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    private int mode = NONE;

    private PointF mStartPoint = new PointF();
    private PointF mMiddlePoint = new PointF();
    private Point leftTopPoint = new Point();

    private float oldDist = 1f;
    private float matrixValues[] = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private float scale;
    private float oldEventX = 0;
    private float oldEventY = 0;
    private float oldStartPointX = 0;
    private float oldStartPointY = 0;
    private int mViewWidth = -1;
    private int mViewHeight = -1;
    private int mBitmapWidth = -1;
    private int mBitmapHeight = -1;
    private boolean mDraggable = false;


    public MyImageView(Context context) {
        this(context, null, 0);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnTouchListener(this);
        gestureDetector = new GestureDetector(getContext(), simpleOnGestureListener);
        gestureDetector.setOnDoubleTapListener(simpleOnGestureListener);
    }

    private GestureDetector gestureDetector;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener =
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    if (actionListener != null) {
                        actionListener.onTouchDown();
                    }
                    return super.onDown(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    if (actionListener != null) {
                        actionListener.onLongPress();
                    }
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (actionListener != null) {
                        actionListener.onDoubleTap();
                    }
                    return super.onDoubleTap(e);
                }
            };

    public float getScaleX(Matrix mat) {
        float[] arr = new float[9];
        mat.getValues(arr);

        return arr[Matrix.MSCALE_X];
    }


    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        initBitmap(ScaleType.INSIDE);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        initBitmap(ScaleType.INSIDE);
    }

    private enum ScaleType {
        ORIGIN,
        INSIDE,
        CUSTOM
    }

    private float customScale = 1.0f;

    private void initBitmap(ScaleType scaleType) {
        if (bitmap != null && mViewWidth > 0 && mViewHeight > 0) {
            setImageBitmap(bitmap);

            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();

            float scaleX = 1.0f * mViewWidth / mBitmapWidth;
            float scaleY = 1.0f * mViewHeight / mBitmapHeight;
            float scale;
            if (scaleType == ScaleType.INSIDE) {
                //nothing
                scale = scaleX < scaleY ? scaleX : scaleY;
            } else if (scaleType == ScaleType.CUSTOM) {
                scale = customScale;
            } else {
                //origin
                scale = 1.0f;
            }
            matrix.setScale(scale, scale);

            if (scaleX < scaleY) {
                leftTopPoint.x = 0;
                leftTopPoint.y = (int) (mViewHeight - mBitmapHeight * scale) / 2;
            } else {
                leftTopPoint.x = (int) (mViewWidth - mBitmapWidth * scale) / 2;
                leftTopPoint.y = 0;
            }

            matrix.postTranslate(leftTopPoint.x, leftTopPoint.y);

            this.setImageMatrix(matrix);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                mStartPoint.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mMiddlePoint, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    drag(event);
                } else if (mode == ZOOM) {
                    zoom(event);
                }
                break;
        }

        gestureDetector.onTouchEvent(event);
        return true;
    }


    public void drag(MotionEvent event) {
        matrix.getValues(matrixValues);

        float left = matrixValues[2];
        float top = matrixValues[5];
        float bottom = (top + (matrixValues[0] * mBitmapHeight)) - mViewHeight;
        float right = (left + (matrixValues[0] * mBitmapWidth)) - mViewWidth;

        float eventX = event.getX();
        float eventY = event.getY();
        float spacingX = eventX - mStartPoint.x;
        float spacingY = eventY - mStartPoint.y;
        float newPositionLeft = (left < 0 ? spacingX : spacingX * -1) + left;
        float newPositionRight = (spacingX) + right;
        float newPositionTop = (top < 0 ? spacingY : spacingY * -1) + top;
        float newPositionBottom = (spacingY) + bottom;
        boolean x = true;
        boolean y = true;

        if (newPositionRight < 0.0f || newPositionLeft > 0.0f) {
            if (newPositionRight < 0.0f && newPositionLeft > 0.0f) {
                x = false;
            } else {
                eventX = oldEventX;
                mStartPoint.x = oldStartPointX;
            }
        }
        if (newPositionBottom < 0.0f || newPositionTop > 0.0f) {
            if (newPositionBottom < 0.0f && newPositionTop > 0.0f) {
                y = false;
            } else {
                eventY = oldEventY;
                mStartPoint.y = oldStartPointY;
            }
        }

        if (mDraggable) {
            matrix.set(savedMatrix);
            matrix.postTranslate(x ? eventX - mStartPoint.x : 0, y ? eventY - mStartPoint.y : 0);
            this.setImageMatrix(matrix);
            if (x) oldEventX = eventX;
            if (y) oldEventY = eventY;
            if (x) oldStartPointX = mStartPoint.x;
            if (y) oldStartPointY = mStartPoint.y;
        }

    }

    public void zoom(MotionEvent event) {
        matrix.getValues(matrixValues);

        float newDist = spacing(event);
        float bitmapWidth = matrixValues[0] * mBitmapWidth;
        float bimtapHeight = matrixValues[0] * mBitmapHeight;
        boolean in = newDist > oldDist;

        if (!in && matrixValues[0] < 1) {
            return;
        }
        if (bitmapWidth > mViewWidth || bimtapHeight > mViewHeight) {
            mDraggable = true;
        } else {
            mDraggable = false;
        }

        float midX = (mViewWidth / 2);
        float midY = (mViewHeight / 2);

        matrix.set(savedMatrix);
        scale = newDist / oldDist;
        matrix.postScale(scale, scale, bitmapWidth > mViewWidth ? mMiddlePoint.x : midX, bimtapHeight > mViewHeight ? mMiddlePoint.y : midY);

        customScale = getScaleX(matrix);
        this.setImageMatrix(matrix);
    }


    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    public interface ActionListener {
        void onTouchDown();

        void onLongPress();

        void onDoubleTap();
    }
}
