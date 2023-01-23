package com.hipla.channel.foodModule.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.hipla.channel.foodModule.viewmodel.*
import com.hipla.channel.viewmodel.FeedbackViewmodel
import com.hipla.channel.viewmodel.GenerateOtpViewmodel
import com.hipla.channel.viewmodel.UnitAvailabiltyViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.viewmodel.OrderPlaceMeetingRoomViewModel
import com.hipla.sentinelvms.sentinelKt.foodModule.viewmodel.QuickSettingsViewModel

  class CommonFactory (private val repository: CommonRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AllPantryViewModel::class.java)) {
            AllPantryViewModel(this.repository) as T
        } else if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            FoodViewModel(this.repository) as T
        }  else if (modelClass.isAssignableFrom(PantryViewModel::class.java)) {
            PantryViewModel(this.repository) as T
        }  else if (modelClass.isAssignableFrom(OrderPlaceViewModel::class.java)) {
            OrderPlaceViewModel(this.repository) as T
        } else if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            OrderHistoryViewModel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(OrderPlaceMeetingRoomViewModel::class.java)) {
            OrderPlaceMeetingRoomViewModel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(QuickSettingsViewModel::class.java)) {
            QuickSettingsViewModel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(UnitAvailabiltyViewModel::class.java)) {
            UnitAvailabiltyViewModel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(FeedbackViewmodel::class.java)) {
            FeedbackViewmodel(this.repository) as T
        }
        else if (modelClass.isAssignableFrom(GenerateOtpViewmodel::class.java)) {
            GenerateOtpViewmodel(this.repository) as T
        }




        else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}