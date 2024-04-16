package com.example.findmyfriends.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.findmyfriends.data.FirebaseServices
import com.example.findmyfriends.data.model.FMFGroup
import com.example.findmyfriends.data.model.FMFUser
import com.example.findmyfriends.ui.composables.makeToastText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FirebaseDataViewModel: ViewModel() {
    companion object {
        val Factory = viewModelFactory {
            initializer {
                FirebaseServices.updateAuth()
                FirebaseDataViewModel()
            }
        }
    }
    var currentUser = FirebaseServices.currentUser
    var currentGroupId = ""
    lateinit var user: FMFUser //= FMFUser(currentUser!!.displayName!!, currentUser.email!!, currentUser.uid,)
    var groups : MutableLiveData<HashMap<String, FMFGroup>> = MutableLiveData()// MutableLiveData(HashMap<String,FMFGroup>())  // HashMap<String,FMFGroup>()
    var users : MutableLiveData<HashMap<String, FMFUser>> = MutableLiveData()
    var currentGroup: MutableLiveData<FMFGroup> = MutableLiveData()

    init {
        if (currentUser != null) {
            user = FMFUser(currentUser!!.displayName!!, currentUser!!.email!!, currentUser!!.uid)

        }
    }

    suspend fun updateUserProfile(email: String, username: String, currentPassword: String, newPassword: String,): String {
        var message = ""
        val credential = EmailAuthProvider.getCredential(currentUser?.email!!, currentPassword)

        viewModelScope.launch(Dispatchers.IO) {
            runBlocking {
                try{
                    Log.d("TAGUPDATEUSER","BEFORE REAUTH")
                    currentUser?.reauthenticate(credential)?.await()//.addOnSuccessListener {
                        //Update email
                        Log.d("TAGUPDATEUSER","REAUTHENTICATED BEGIN")
                            runBlocking {
                                if (email!=user.email){

                                    viewModelScope.launch(Dispatchers.IO) {
                                        runBlocking {
                                            currentUser!!.verifyBeforeUpdateEmail(email)
                                                .addOnSuccessListener {
                                                    message += "Check your email inbox for a verification email.\n"
                                                }
                                                .addOnFailureListener {
                                                    message += "ERROR: ${it.message!!.split(".")[0]}\n"
                                                }.await()
                                        }
                                }.join()
                            }
                                //Update username
                                if (username!= currentUser!!.displayName){
                                    viewModelScope.launch(Dispatchers.IO) {
                                        runBlocking {
                                            currentUser!!.updateProfile(
                                                userProfileChangeRequest {
                                                    displayName = username
                                                })
                                                .addOnSuccessListener {
                                                    FirebaseServices.updateCurrentUserUsername(username)
                                                    message += "Username Updated\n"
                                                }
                                                .addOnFailureListener {
                                                    message += "ERROR: ${it.message!!.split(".")[0]}"
                                                }.await()
                                        }

                                    }.join()
                                }
                                //Update password
                                if (newPassword.isNotEmpty()) {
                                    viewModelScope.launch(Dispatchers.IO) {
                                        runBlocking {
                                            currentUser!!.updatePassword(newPassword)
                                                .addOnSuccessListener {
                                                    message += "Password Updated!\n"
                                                }
                                                .addOnFailureListener{
                                                    message += "ERROR: ${it.message!!.split(".")[0]}\n"
                                                }.await()
                                        }

                                    }.join()
                                }
                        }
                }
                catch(e: Exception){
                    Log.d("TAGUPDATEUSER EXCEPTION", e.message.toString())
                    message += e.message.toString()
                }
            }
        }.join()
        Log.d("TAGUPDATEUSER", "MESSAGE: $message")
        FirebaseServices.updateAuth()
        currentUser=FirebaseServices.currentUser
        user = FMFUser(currentUser?.displayName!!, currentUser!!.email!!, currentUser!!.uid)
        return message
    }

    fun addUser(username: String, email: String, password: String) {

    }

    fun loadGroups() {//: MutableMap<String, FMFGroup> {
        if (currentUser != null) {
            var newgroups = HashMap<String, FMFGroup>()
            viewModelScope.launch(Dispatchers.IO) {
                newgroups = FirebaseServices.loadGroups()

                Log.d("AFTERLOAD",newgroups.toString())
                groups.postValue(newgroups)
            }

        }
    }
    fun loadGroup(groupId: String){
        viewModelScope.launch(Dispatchers.IO) {
            var group = FirebaseServices.loadGroup(groupId)
            currentGroup.postValue(group)
            Log.d("LOADEDGROUP", group.toString())
        }
    }
    fun joinGroup(groupID: String){
            viewModelScope.launch(Dispatchers.IO) {
                FirebaseServices.joinGroup(groupID)
                loadGroups()
            }

    }
    fun leaveGroup(groupID: String){
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseServices.leaveGroup(groupID)
        }
    }
    fun loadGroupUsers(groupID: String) {
        var groupUsers = HashMap<String, FMFUser>()
        viewModelScope.launch(Dispatchers.IO) {
            groupUsers = FirebaseServices.loadGroupUsers(groupID)

            Log.d("AFTERLOAD",groupUsers.toString())
            users.postValue(groupUsers)
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseServices.addGroup(name)
            loadGroups()
        }

    }




}
