//
//  CustomEditTextPreference.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomEditTextPreference extends EditTextPreference {

    private int minLength = Integer.MAX_VALUE;

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        updateOKButton(getText().length());
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOKButton(s.length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setup(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomEditTextPreference, 0, 0);
        minLength = a.getInt(R.styleable.CustomEditTextPreference_minLength, Integer.MAX_VALUE);
        a.recycle();
    }

    private void updateOKButton(int textLength) {
        Dialog dialog = getDialog();
        if (null != dialog) {
            Button okButton = (Button) dialog.findViewById(android.R.id.button1);
            okButton.setEnabled(textLength >= minLength);
        }
    }
}
