package com.denchic45.kts;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class InstantAutoComplete extends AppCompatAutoCompleteTextView {

    public InstantAutoComplete(Context context) {
        super(context);
        init();
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        init();
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        init();
        showDropDown();
    }

    private void init() {
//        setInputType(0);
    }



    public void inputEnable(boolean enabled) {
        setCursorVisible(enabled);
        setFocusable(enabled);
    }


    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        super.performFiltering("", 0);
    }
}