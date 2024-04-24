package com.syndicate.carsharing.viewmodels

import androidx.lifecycle.ViewModel
import com.syndicate.carsharing.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(val userStore: UserStore) : ViewModel() {

}