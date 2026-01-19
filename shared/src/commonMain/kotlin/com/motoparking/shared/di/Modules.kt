package com.motoparking.shared.di

import com.motoparking.shared.data.remote.ParkingDataSource
import com.motoparking.shared.data.remote.SupabaseParkingDataSource
import com.motoparking.shared.data.remote.createSupabaseClient
import com.motoparking.shared.data.repository.ParkingRepository
import org.koin.dsl.module

val sharedModule = module {
    single { createSupabaseClient() }
    single<ParkingDataSource> { SupabaseParkingDataSource(get()) }
    single { ParkingRepository(get()) }
}
