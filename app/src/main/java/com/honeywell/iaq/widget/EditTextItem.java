package com.honeywell.iaq.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;


/**
 * Created by milton on 16/6/7.
 */
public class EditTextItem extends LinearLayout {
    private TextView mName;
    private EditText mEditName;
    private OnEditTextChangedListener mOnEditTextChangedListener;

    public EditTextItem(Context context) {
        super(context);
    }

    public EditTextItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.widget_edit_item, this);
        setOrientation(VERTICAL);
        TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.EditTextItem);
        setTitle(ta.getText(R.styleable.EditTextItem_eti_title));
        setText(ta.getText(R.styleable.EditTextItem_eti_text));
        setHint(ta.getText(R.styleable.EditTextItem_eti_hint));
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initTextName();
        initEditName();
        if (mEditName != null) {
            mEditName.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    if (mOnEditTextChangedListener != null) {
                        mOnEditTextChangedListener.afterTextChanged(s.toString());
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }

    }

    public EditTextItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitle(int resId) {
        initTextName();
        if (mName != null) {
            mName.setText(resId);
        }
    }

    public void setTitle(CharSequence name) {
        initTextName();
        if (mName != null) {
            mName.setText(name);
        }
    }

    public TextView getTitle() {
        return mName;
    }

    public void setText(int resId) {
        initEditName();
        if (mEditName != null) {
            mEditName.setText(resId);
        }
    }

    public void setText(CharSequence name) {
        initEditName();
        if (mEditName != null) {
            mEditName.setText(name);
        }
    }

    public EditText getText() {
        return mEditName;
    }

    private void initEditName() {
        if (mEditName == null) {
            mEditName = (EditText) findViewById(R.id.et_name);
        }
    }

    private void initTextName() {
        if (mName == null) {
            mName = (TextView) findViewById(R.id.tv_name);
        }
    }

    public void setHint(int resId) {
        initEditName();
        if (mEditName != null) {
            mEditName.setHint(resId);
        }
    }
    public void setHint(CharSequence hint) {
        initEditName();
        if (mEditName != null) {
            mEditName.setHint(hint);
        }
    }

    public void setImeOptions(int imeOptions) {
        initEditName();
        if (mEditName != null) {
            mEditName.setImeOptions(imeOptions);
        }
    }
    public void setInputType(int type) {
        initEditName();
        if (mEditName != null) {
            mEditName.setInputType(type);

        }
    }
    public void setOnEditTextChangedListener(OnEditTextChangedListener listener) {
        mOnEditTextChangedListener = listener;
    }

    public interface OnEditTextChangedListener {
        public void afterTextChanged(String s);
    }

    public String getContent() {
        return mEditName != null ? mEditName.getText().toString() : "";
    }

    public int getIntegerContent() {
        if (mEditName == null) {
            return -1;
        } else {
            final String res = mEditName.getText().toString();
            if ((!TextUtils.isEmpty(res)) && TextUtils.isDigitsOnly(res)) {
                return Integer.parseInt(res);
            } else {
                return -1;
            }
        }
    }
}
