/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.wallet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import vandyke.siamobile.R
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.models.consensus.ConsensusModel
import vandyke.siamobile.backend.models.explorer.ExplorerTransactionModel
import vandyke.siamobile.backend.models.wallet.*
import vandyke.siamobile.backend.networking.*
import vandyke.siamobile.prefs
import vandyke.siamobile.util.NotificationUtil
import vandyke.siamobile.util.SCUtil
import vandyke.siamobile.util.round
import vandyke.siamobile.util.toSC
import java.math.BigDecimal

class WalletService : BaseMonitorService() {

    private var csMode: Boolean = true
        get() = prefs.operationMode == "cold_storage"

    private var seed: String = prefs.coldStorageSeed
    private var addresses: ArrayList<String> = ArrayList(prefs.coldStorageAddresses)
    private var password: String = prefs.coldStoragePassword
    private var exists: Boolean = prefs.coldStorageExists
    private var unlocked: Boolean = false
    private var height: Long = 0

    private val TRANSACTION_NOTIFICATION: Int = 3
    private val SYNC_NOTIFICATION: Int = 2

    private var listeners: ArrayList<WalletUpdateListener> = ArrayList()

    override fun refresh() {
        if (csMode) {
            Explorer.siaTech(SiaCallback({ it ->
                if (height != it.height) {
                    height = it.height
                    val confirmedTxs: ArrayList<TransactionModel> = ArrayList()
                    var balance = BigDecimal.ZERO
//                addresses = arrayListOf("20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3", "4c06e08c8689625ddf9831415706529673077325d08e9c16be401d348270937c2db7e284a57f")
                    for (address in addresses) { // TODO: make this blocking
                        Explorer.siaTechHash(address, SiaCallback({ it ->
                            for (tx in it.transactions) {
                                val txModel = tx.toTransactionModel()
                                balance += txModel.netValue
                                confirmedTxs += txModel
                            }
                        }, {
                            if (it.reason != SiaError.Reason.UNRECOGNIZED_HASH) sendTransactionsError(it)
                        }))
                    }
                    sendBalanceUpdate(WalletModel(exists, unlocked, false, balance))
                    sendTransactionsUpdate(TransactionsModel(confirmedTxs))
                }
                sendSyncUpdate(ConsensusModel(true, it.height))
            }, {
                sendSyncError(it)
            }))
        } else {
            refreshBalanceAndStatus()
            refreshTransactions()
            refreshConsensus()
        }
    }

    fun refreshBalanceAndStatus() {
        Wallet.wallet(SiaCallback({ it -> sendBalanceUpdate(it) }, { sendBalanceError(it) }))
        Wallet.scPrice(SiaCallback({ it -> sendUsdUpdate(it) }, { sendUsdError(it) }))
    }

    fun refreshTransactions() {
        Wallet.transactions(SiaCallback({ it ->
            sendTransactionsUpdate(it)
            val mostRecentTxId = prefs.mostRecentTxId // TODO: can give false positives when switching between wallets
            var newTxs = 0
            var netOfNewTxs = BigDecimal.ZERO
            for (tx in it.alltransactions) {
                if (tx.transactionid == mostRecentTxId) {
                    break
                } else {
                    newTxs++
                    netOfNewTxs = netOfNewTxs.add(tx.netValue)
                }
            }
            if (newTxs > 0) {
                prefs.mostRecentTxId = it.alltransactions[0].transactionid
                NotificationUtil.notification(this@WalletService, TRANSACTION_NOTIFICATION,
                        R.drawable.ic_account_balance, newTxs.toString() + " new transaction" + if (newTxs > 1) "s" else "",
                        "Net value: " + (if (netOfNewTxs > BigDecimal.ZERO) "+" else "") + netOfNewTxs.toSC().round().toPlainString() + " SC",
                        false)
            }
        }, { sendTransactionsError(it) }))
    }

    fun refreshConsensus() {
        Consensus.consensus(SiaCallback({ it ->
            sendSyncUpdate(it)
            if (it.syncprogress == 0.0 || it.synced) {
                NotificationUtil.cancelNotification(this@WalletService, SYNC_NOTIFICATION)
            } else {
                NotificationUtil.notification(this@WalletService, SYNC_NOTIFICATION, R.drawable.ic_sync,
                        "Syncing...", String.format("Progress (estimated): %.2f%%", it.syncprogress), false)
            }
        }, { sendSyncError(it) }))
    }

    fun wallet(callback: SiaCallback<WalletModel>) {
        if (csMode)
            if (!exists) {
                callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
            } else {
                callback.onSuccess?.invoke(WalletModel(exists, unlocked))
            }
    }

    fun lock(callback: SiaCallback<Unit>) {
        if (csMode) {
            unlocked = false
            callback.onSuccessNull?.invoke()
        } else
            Wallet.lock(callback)
    }

    fun unlock(password: String, callback: SiaCallback<Unit>) {
        if (csMode) {
            if (!exists) {
                callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
            } else if (password == this.password) {
                unlocked = true
            } else
                callback.onError(SiaError(SiaError.Reason.WALLET_PASSWORD_INCORRECT))
        } else
            Wallet.unlock(password, callback)
    }

    fun address(callback: SiaCallback<AddressModel>) {
        if (csMode) {
            if (csExistsUnlocked(callback.onError))
                callback.onSuccess?.invoke(AddressModel(addresses[(Math.random() * addresses.size).toInt()]))
        } else {
            Wallet.address(callback)
        }
    }

    fun addresses(callback: SiaCallback<AddressesModel>) {
        if (csMode) {
            if (csExistsUnlocked(callback.onError))
                callback.onSuccess?.invoke(AddressesModel(addresses))
        } else
            Wallet.addresses(callback)
    }

    fun csExistsUnlocked(onError: (SiaError) -> Unit): Boolean {
        if (!exists) {
            onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
            return false
        } else if (!unlocked) {
            onError(SiaError(SiaError.Reason.WALLET_LOCKED))
            return false
        } else
            return true
    }

    interface WalletUpdateListener {
        fun onBalanceUpdate(walletModel: WalletModel)
        fun onUsdUpdate(scPriceModel: ScPriceModel)
        fun onTransactionsUpdate(transactionsModel: TransactionsModel)
        fun onSyncUpdate(consensusModel: ConsensusModel)
        fun onBalanceError(error: SiaError)
        fun onUsdError(error: SiaError)
        fun onTransactionsError(error: SiaError)
        fun onSyncError(error: SiaError)
    }

    fun registerListener(listener: WalletUpdateListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: WalletUpdateListener) {
        listeners.remove(listener)
    }

    fun sendBalanceUpdate(walletModel: WalletModel) {
        for (listener in listeners)
            listener.onBalanceUpdate(walletModel)
    }

    fun sendUsdUpdate(scPriceModel: ScPriceModel) {
        for (listener in listeners)
            listener.onUsdUpdate(scPriceModel)
    }

    fun sendTransactionsUpdate(transactionsModel: TransactionsModel) {
        for (listener in listeners)
            listener.onTransactionsUpdate(transactionsModel)
    }

    fun sendSyncUpdate(consensusModel: ConsensusModel) {
        for (listener in listeners)
            listener.onSyncUpdate(consensusModel)
    }

    fun sendBalanceError(error: SiaError) {
        for (listener in listeners)
            listener.onBalanceError(error)
    }

    fun sendUsdError(error: SiaError) {
        for (listener in listeners)
            listener.onUsdError(error)
    }

    fun sendTransactionsError(error: SiaError) {
        for (listener in listeners)
            listener.onTransactionsError(error)
    }

    fun sendSyncError(error: SiaError) {
        for (listener in listeners)
            listener.onSyncError(error)
    }

    fun ExplorerTransactionModel.toTransactionModel(): TransactionModel {
        val inputsList = ArrayList<TransactionInputModel>()
        for (input in siacoininputoutputs)
            inputsList.add(TransactionInputModel(walletaddress = addresses.contains(input.unlockhash), value = input.value))
        val outputsList = ArrayList<TransactionOutputModel>()
        for (output in rawtransaction.siacoinoutputs)
            outputsList.add(TransactionOutputModel(walletaddress = addresses.contains(output.unlockhash), value = output.value))
        return TransactionModel(id, height, SCUtil.estimatedTimeAtHeight(height), inputsList, outputsList)
    }

    companion object {
        fun singleAction(context: Context, action: (service: WalletService) -> Unit) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    action((service as LocalBinder).service as WalletService)
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                }
            }
            context.bindService(Intent(context, WalletService::class.java), connection, Context.BIND_AUTO_CREATE)
        }
    }
}
