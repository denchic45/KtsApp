package com.denchic45.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.denchic45.kts.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MovableFloatingActionButton extends FloatingActionButton
//        implements View.OnTouchListener
{

    public final static float CLICK_DRAG_TOLERANCE = 100; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.

    private float downRawX, downRawY;
    private float dX, dY;
    private FabMoveListener moveListener;
    private OnTouchListener onTouchListener;
    private FrameLayout frameLayout;

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setMoveListener(FabMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    private void init() {
//        setOnTouchListener(this);
        post(() -> {
            frameLayout = new FrameLayout(getContext());
//            setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            frameLayout.setBackgroundColor(getContext().getColor(R.color.blue));
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(getLayoutParams()));
            ViewGroup parent = (ViewGroup) getParent();
            parent.addView(frameLayout);
//            parent.removeView(this);
//            frameLayout.addView(this);


            frameLayout.setOnTouchListener((view, motionEvent) -> {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {

                    downRawX = motionEvent.getRawX();
                    downRawY = motionEvent.getRawY();
                    dX = view.getX() - downRawX;
                    dY = view.getY() - downRawY;

                    return true; // Consumed

                } else if (action == MotionEvent.ACTION_MOVE) {

                    int viewWidth = view.getWidth();
                    int viewHeight = view.getHeight();

                    View viewParent = (View) view.getParent();
                    int parentWidth = viewParent.getWidth();
                    int parentHeight = viewParent.getHeight();

                    float newX = motionEvent.getRawX() + dX;
                    newX = Math.max(layoutParams.leftMargin, newX); // Don't allow the FAB past the left hand side of the parent
                    newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX); // Don't allow the FAB past the right hand side of the parent

                    float newY = motionEvent.getRawY() + dY;
                    newY = Math.max(layoutParams.topMargin, newY); // Don't allow the FAB past the top of the parent
                    newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY); // Don't allow the FAB past the bottom of the parent

                    view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start();

                    return true; // Consumed

                } else if (action == MotionEvent.ACTION_UP) {

                    float upRawX = motionEvent.getRawX();
                    float upRawY = motionEvent.getRawY();

                    float upDX = upRawX - downRawX;
                    float upDY = upRawY - downRawY;

                    if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                        return performClick();
                    } else { // A drag
                        return true; // Consumed
                    }

                } else {
//                    return super.onTouchEvent(motionEvent);
                    return false;
                }
            });
        });
    }

//    @Override
//    public boolean onTouch(@NotNull View view, @NotNull MotionEvent motionEvent) {
//        onTouchListener.onTouch(view, motionEvent);
//        moveListener.onEvent(motionEvent);

//    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
        onTouchListener = l;
    }

    public interface FabMoveListener {
        void onEvent(MotionEvent event);
    }

}
