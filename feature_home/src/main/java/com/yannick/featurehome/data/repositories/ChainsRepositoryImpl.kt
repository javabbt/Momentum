package com.yannick.featurehome.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yannick.domain.models.Chain
import com.yannick.featurehome.data.paging.ChainsPagingSource
import com.yannick.featurehome.domain.repositories.ChainsRepository
import kotlinx.coroutines.flow.Flow

class ChainsRepositoryImpl(
    private val source: ChainsPagingSource,
    private val config: PagingConfig,
) : ChainsRepository {
    override fun getChains(): Flow<PagingData<Chain>> {
        return Pager(
            config = config,
            pagingSourceFactory = { source },
        ).flow
    }
}
