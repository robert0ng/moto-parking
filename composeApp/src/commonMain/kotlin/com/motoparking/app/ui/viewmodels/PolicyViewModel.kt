package com.motoparking.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motoparking.shared.data.repository.ParkingRepository
import com.motoparking.shared.domain.model.PolicyZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class PolicyGroup(
    val city: String,
    val effectiveDate: LocalDate,
    val scope: String,
    val districts: List<String>,
    val feeDescription: String?,
    val sourceUrl: String?,
    val sourceLabel: String?,
    val daysUntil: Int
)

data class PolicyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val alreadyOpen: List<PolicyGroup> = emptyList(),
    val nowOpen: List<PolicyGroup> = emptyList(),
    val upcoming: List<PolicyGroup> = emptyList(),
    val rawZones: List<PolicyZone> = emptyList()
)

class PolicyViewModel(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PolicyUiState())
    val uiState: StateFlow<PolicyUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    @OptIn(ExperimentalTime::class)
    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val zones = repository.getAllPolicyZones()
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val cutoffPast = today.minus(DatePeriod(days = 30))

                val groups = zones
                    .groupBy { Triple(it.city, it.effectiveDate, it.scope) }
                    .map { (key, list) ->
                        val (city, effDate, scope) = key
                        val sample = list.first()
                        PolicyGroup(
                            city = city,
                            effectiveDate = effDate,
                            scope = scope,
                            districts = list.map { it.district ?: "全市" },
                            feeDescription = sample.feeDescription,
                            sourceUrl = sample.sourceUrl,
                            sourceLabel = sample.sourceLabel,
                            daysUntil = today.daysUntil(effDate)
                        )
                    }
                    .sortedBy { it.effectiveDate }

                _uiState.value = PolicyUiState(
                    isLoading = false,
                    alreadyOpen = groups.filter { it.effectiveDate < cutoffPast },
                    nowOpen = groups.filter {
                        it.effectiveDate >= cutoffPast && it.effectiveDate <= today
                    },
                    upcoming = groups.filter { it.effectiveDate > today },
                    rawZones = zones
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "載入失敗"
                )
            }
        }
    }

    fun retry() = load()

    fun findPoliciesForAddress(address: String): List<PolicyZone> =
        repository.findPoliciesForAddress(address, _uiState.value.rawZones)
}
