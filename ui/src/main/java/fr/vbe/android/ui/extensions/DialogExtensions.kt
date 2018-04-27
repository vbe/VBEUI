package fr.vbe.android.ui.extensions

import android.support.v7.app.AlertDialog

fun AlertDialog.Builder.setPositiveBtn(textId: Int, action: () -> Unit): AlertDialog.Builder {
    setPositiveButton(textId, { _, _ -> action() })
    return this
}

fun AlertDialog.Builder.setPositiveBtn(text: CharSequence, action: () -> Unit): AlertDialog.Builder {
    setPositiveButton(text, { _, _ -> action() })
    return this
}

fun AlertDialog.Builder.setNegativeBtn(textId: Int, action: () -> Unit): AlertDialog.Builder {
    setNegativeButton(textId, { _, _ -> action() })
    return this
}

fun AlertDialog.Builder.setNegativeBtn(text: CharSequence, action: () -> Unit): AlertDialog.Builder {
    setNegativeButton(text, { _, _ -> action() })
    return this
}

fun AlertDialog.Builder.setNeutralBtn(textId: Int, action: () -> Unit): AlertDialog.Builder {
    setNeutralButton(textId, { _, _ -> action() })
    return this
}

fun AlertDialog.Builder.setNeutralBtn(text: CharSequence, action: () -> Unit): AlertDialog.Builder {
    setNeutralButton(text, { _, _ -> action() })
    return this
}