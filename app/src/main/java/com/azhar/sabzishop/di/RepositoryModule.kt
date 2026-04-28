package com.azhar.sabzishop.di

import com.azhar.sabzishop.data.repository.AuthRepositoryImpl
import com.azhar.sabzishop.data.repository.CartRepositoryImpl
import com.azhar.sabzishop.data.repository.FeedbackRepositoryImpl
import com.azhar.sabzishop.data.repository.OrderRepositoryImpl
import com.azhar.sabzishop.data.repository.ProductRepositoryImpl
import com.azhar.sabzishop.domain.repository.AuthRepository
import com.azhar.sabzishop.domain.repository.CartRepository
import com.azhar.sabzishop.domain.repository.FeedbackRepository
import com.azhar.sabzishop.domain.repository.OrderRepository
import com.azhar.sabzishop.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain repository interfaces to their data layer implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository
}

