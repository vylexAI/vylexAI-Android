package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.auth.AuthTokenStore
import com.vylexai.app.data.net.VylexException
import com.vylexai.app.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the Wallet screen.
 *
 * On a fresh install there is no JWT yet, so we can't call the coordinator.
 * In that state we surface a transparent "demo" snapshot so the UI stays
 * beautiful and investors see reasonable numbers. The moment login lands
 * (SMI-112), the same view model flips to live balances with no UI change.
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val wallet: WalletRepository,
    private val tokenStore: AuthTokenStore
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState(kind = WalletKind.Loading))
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (!tokenStore.isAuthenticated()) {
            _state.value = DEMO_STATE
            return
        }
        _state.update { it.copy(kind = WalletKind.Loading) }
        viewModelScope.launch {
            runCatching { wallet.balance() }
                .onSuccess { r ->
                    _state.value = WalletUiState(
                        kind = WalletKind.Live,
                        balanceBsai = BigDecimal(r.balanceBsai),
                        totalCredited = BigDecimal(r.totalCreditedBsai),
                        totalDebited = BigDecimal(r.totalDebitedBsai)
                    )
                }
                .onFailure { t ->
                    _state.value = when (t) {
                        is VylexException.AuthExpired -> DEMO_STATE
                        else -> WalletUiState(kind = WalletKind.Error, message = t.message)
                    }
                }
        }
    }

    private companion object {
        val DEMO_STATE = WalletUiState(
            kind = WalletKind.Demo,
            balanceBsai = BigDecimal("24.38"),
            totalCredited = BigDecimal("28.90"),
            totalDebited = BigDecimal("4.52")
        )
    }
}

enum class WalletKind { Loading, Live, Demo, Error }

data class WalletUiState(
    val kind: WalletKind,
    val balanceBsai: BigDecimal = BigDecimal.ZERO,
    val totalCredited: BigDecimal = BigDecimal.ZERO,
    val totalDebited: BigDecimal = BigDecimal.ZERO,
    val message: String? = null
)
