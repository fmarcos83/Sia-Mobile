/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_send.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.ui.wallet.ScannerActivity
import vandyke.siamobile.util.SnackbarUtil
import vandyke.siamobile.util.toHastings

class WalletSendDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_send

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseListener(walletSendCancel)
        sendAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // change miner fee text
            }
            override fun afterTextChanged(s: Editable) {}
        })
        walletSend.setOnClickListener {
            val sendAmount = sendAmount.text.toString().toHastings().toPlainString()
            Wallet.send(sendAmount, sendRecipient.text.toString(), SiaCallback({ ->
                SnackbarUtil.successSnackbar(view)
                close()
            }, {
                it.snackbar(view)
            }))
        }

        walletScan.setOnClickListener { startScannerActivity() }
    }

    private fun startScannerActivity() {
        startActivityForResult(Intent(activity, ScannerActivity::class.java), SCAN_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SCAN_REQUEST) {
            sendRecipient.setText(data?.getStringExtra(SCAN_RESULT_KEY))
        }
    }

    companion object {
        private val SCAN_REQUEST = 20
        val SCAN_RESULT_KEY = "SCAN_RESULT"
    }
}
