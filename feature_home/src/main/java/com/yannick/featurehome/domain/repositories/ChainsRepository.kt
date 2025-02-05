package com.yannick.featurehome.domain.repositories

import androidx.paging.PagingData
import com.yannick.domain.models.Chain
import kotlinx.coroutines.flow.Flow

interface ChainsRepository {
    fun getChains(): Flow<PagingData<Chain>>
}
