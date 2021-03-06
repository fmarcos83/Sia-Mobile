package vandyke.siamobile.backend.models.wallet

data class TransactionsModel(val confirmedtransactions: ArrayList<TransactionModel>? = ArrayList(),
                             val unconfirmedtransactions: ArrayList<TransactionModel>? = ArrayList()) {
    val alltransactions by lazy { (confirmedtransactions?: ArrayList()) + (unconfirmedtransactions?: ArrayList()) }
}