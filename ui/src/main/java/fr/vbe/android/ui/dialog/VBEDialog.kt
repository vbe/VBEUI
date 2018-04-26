package fr.vbe.android.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import fr.vbe.android.base.text.VBELabel

class VBEDialog(
        context: Context,
        val title: VBELabel? = null,
        val message: VBELabel? = null,
        val positive: ButtonConfig?,
        val negative: ButtonConfig?,
        val neutral: ButtonConfig?) {


    val listener = DialogInterface.OnClickListener { dialog, which ->
        when(which) {
            Dialog.BUTTON_POSITIVE -> positive?.executeAction(dialog)
            Dialog.BUTTON_NEGATIVE -> negative?.executeAction(dialog)
            Dialog.BUTTON_NEUTRAL -> neutral?.executeAction(dialog)
        }
    }

    val builder = AlertDialog.Builder(context).also { builder ->
        title?.let { builder.setTitle(it.get(context)) }
        message?.let{ builder.setMessage(it.get(context)) }

        positive?.let { builder.setPositiveButton(it.label.get(context), listener) }
        negative?.let { builder.setNegativeButton(it.label.get(context), listener) }
        neutral?.let { builder.setNeutralButton(it.label.get(context), listener) }
    }

    fun show() {
        builder.show()
    }

    class ButtonConfig {
        val label: VBELabel
        private val action: () -> Unit
        private val dismiss: Boolean

        constructor(label: VBELabel, action: () -> Unit, dismiss: Boolean = true) {
            this.label = label
            this.action = action
            this.dismiss = dismiss
        }
        constructor(labelResId: Int, action: () -> Unit, dismiss: Boolean = true) : this(VBELabel(labelResId), action)
        constructor(label: String, action: () -> Unit, dismiss: Boolean = true) : this(VBELabel(label), action)

        fun executeAction(dialog: DialogInterface) {
            action()
            if (dismiss) dialog.dismiss()
        }
    }
}