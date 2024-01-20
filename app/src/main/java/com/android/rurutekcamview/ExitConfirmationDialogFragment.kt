package com.android.rurutekcamview

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ExitConfirmationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = SpannableString("Are you sure want to exit?")
        val negative = SpannableString("Yes")
        val positive = SpannableString("No")

        message.setSpan(ForegroundColorSpan(Color.rgb(220, 141, 23)), 0, message.length, 0)
        positive.setSpan(ForegroundColorSpan(Color.rgb(220, 141, 23)), 0, positive.length, 0)
        negative.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 0)), 0, negative.length, 0)

        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setMessage(message)
            .setPositiveButton(
                positive
            ) { _: DialogInterface?, _: Int -> dismiss() }
            .setNegativeButton(
                negative
            ) { _: DialogInterface?, _: Int -> requireActivity().finishAffinity() }

        return builder.create()
    }
}

