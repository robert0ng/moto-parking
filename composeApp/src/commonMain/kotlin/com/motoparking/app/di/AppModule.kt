package com.motoparking.app.di

import com.motoparking.app.ui.screens.ParkingListViewModel
import com.motoparking.app.ui.viewmodels.DetailViewModel
import com.motoparking.shared.di.sharedModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ParkingListViewModel)
    viewModelOf(::DetailViewModel)
}

// All modules to be loaded
val allModules = listOf(sharedModule, appModule)
