package com.dorcaapps.android.ktorclient.ui.shared

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.BindingAdapter

@BindingAdapter("onImeOptionButtonClick")
fun onImeOptionsButtonClick(editText: EditText, onClick: () -> Unit) {
    editText.setOnEditorActionListener { view, _, _ ->
        val inputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        onClick()
        true
    }
}