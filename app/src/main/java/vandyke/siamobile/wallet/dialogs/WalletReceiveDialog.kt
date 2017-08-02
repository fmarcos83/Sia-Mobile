/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_receive.*
import net.glxn.qrgen.android.QRCode
import vandyke.siamobile.R
import vandyke.siamobile.api.networking.SiaCallback
import vandyke.siamobile.api.networking.Wallet
import vandyke.siamobile.util.SnackbarUtil

class WalletReceiveDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_receive

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletQrCode.visibility = View.INVISIBLE

        Wallet.address(SiaCallback({
            SnackbarUtil.successSnackbar(view)
            receiveAddress.text = it.address
            setQrCode(it.address)
        }, {
            it.snackbar(view)
            receiveAddress.text = "${it.reason.msg}\n"
        }))

        walletAddressCopy.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("receive address", receiveAddress.text)
            clipboard.primaryClip = clip
            SnackbarUtil.snackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT)
            close()
        }
        setCloseListener(walletReceiveClose)
    }

    fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}
