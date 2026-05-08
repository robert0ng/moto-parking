package com.motoparking.app.di

import com.motoparking.app.ui.screens.ParkingListViewModel
import com.motoparking.app.ui.viewmodels.AuthViewModel
import com.motoparking.app.ui.viewmodels.DetailViewModel
import com.motoparking.app.ui.viewmodels.HomeViewModel
import com.motoparking.app.ui.viewmodels.PolicySegmentViewModel
import com.motoparking.app.ui.viewmodels.PolicyViewModel
import com.motoparking.shared.di.sharedModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ParkingListViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::PolicyViewModel)
    viewModelOf(::PolicySegmentViewModel)
}

// All modules to be loaded
val allModules = listOf(sharedModule, appModule)
