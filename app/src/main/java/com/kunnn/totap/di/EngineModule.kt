package com.kunnn.totap.di

import com.kunnn.totap.core.autoclick.controller.AutoClickControllerImpl
import com.kunnn.totap.core.autoclick.permission.AndroidPermissionChecker
import com.kunnn.totap.core.domain.autoclick.AutoClickController
import com.kunnn.totap.core.domain.permission.PermissionChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the engine + permission interfaces to their Android implementations.
 * Implementation classes are Singletons in `:core:autoclick`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {
    @Binds
    @Singleton
    abstract fun bindAutoClickController(impl: AutoClickControllerImpl): AutoClickController

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(impl: AndroidPermissionChecker): PermissionChecker
}
