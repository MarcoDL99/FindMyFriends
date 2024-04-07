package com.example.findmyfriends.data

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.findmyfriends.MainMenuActivity
import com.example.findmyfriends.data.model.FMFGroup
import com.example.findmyfriends.data.model.FMFUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object FirebaseServices {


    var auth = FirebaseAuth.getInstance()
    var currentUser = auth.currentUser
    val db = Firebase.firestore

    fun updateAuth(){
        auth=FirebaseAuth.getInstance()
        currentUser = auth.currentUser
    }
    suspend fun login(email:String, password:String): String{
        var message = ""
        var coroutineScope= CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            runBlocking {
                    try{

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                Log.d("LOGINTAG SUCCESS", "LOGIN SUCCESS")
                                updateAuth()

                            }
                            .addOnFailureListener(){
                                Log.d("LOGINTAG FAIL", "LOGIN FAILURE")
                            }
                            .await()
                    }
                    catch(e: Exception){
                        Log.d("LOGINTAG EXCEPTION", e.message.toString())
                        message += e.message.toString()
                    }

            }
        }.join()
        Log.d("LOGINTAG END", "LOGIN END")
        return message


    }

    fun addUser(user: FMFUser){

        val groups = ArrayList(user.groups)
        val groupsHash = hashMapOf(
            "groups" to groups,
            "name" to user.username
        )
        db.collection("users").document(user.id).set(groupsHash)
            .addOnSuccessListener {  documentReference ->
                updateAuth()

            }
            .addOnFailureListener { e ->
            }
    }
    fun updateCurrentUserUsername(name: String){
        db.collection("users").document(currentUser!!.uid).update(mapOf(
            "name" to name
        ))
    }
    fun addGroup(name: String) : String{
         val group = FMFGroup()
         group.name = name
         group.adminId = auth.currentUser!!.uid
         group.userIDs= mutableListOf(auth.currentUser!!.uid)
         var message = ""
         if (name.length>1){
         runBlocking {
            launch {
                try{
                    val docRef = db.collection("groups").add(group).await()
                    try{
                        db.collection("users").document(auth.currentUser!!.uid).update("groups",FieldValue.arrayUnion(docRef.id)).await()
                    }
                    catch (e: Exception){
                        message += "ERROR: ${e.message!!.split(".")[0]}\n"
                    }
                }
                catch (e: Exception){
                    message += "ERROR: ${e.message!!.split(".")[0]}\n"
                }

            }
        }
         }
         else {
             message = "ERROR: GROUP NAME IS TOO SHORT"
         }
        return message

    }
    suspend fun leaveGroup(groupID: String){
        var message =""
        runBlocking {
            launch {
                val groupDoc = db.collection("groups").document(groupID).get().await()
                if (groupDoc.exists()){
                    val group = groupDoc.toObject<FMFGroup>()
                    launch{
                        try{
                            db.collection("groups").document(groupID).update("userIDs", FieldValue.arrayRemove(
                                currentUser?.uid)).await()
                            db.collection("users").document(currentUser?.uid!!).update("groups", FieldValue.arrayRemove(groupID))

                        }
                        catch (e: Exception){
                            message += "ERROR: ${e.message!!.split(".")[0]}\n"
                        }
                    }
                    if (group?.adminId == currentUser?.uid){
                        launch {
                            try{
                                db.collection("groups").document(groupID).delete().await()

                            }
                            catch (e: Exception){
                                message += "ERROR: ${e.message!!.split(".")[0]}\n"
                            }
                        }
                        group?.userIDs?.forEach{
                            if (it != currentUser?.uid){
                                launch {

                                    try{
                                        db.collection("users").document(it).update("groups", FieldValue.arrayRemove(groupID))


                                    }
                                    catch (e: Exception){
                                        message += "ERROR: ${e.message!!.split(".")[0]}\n"
                                    }
                                }
                            }

                        }

                    }
                }
                else{
                    message = "ERROR: Group does not exist!"
                }
            }
        }
        Log.d("LEAVEGROUP", message)
    }
    fun joinGroup(groupID: String) : String{
        var message = ""
        runBlocking {
                launch {
                    if (db.collection("groups").document(groupID).get().await().exists()){
                        launch{
                            try{
                                db.collection("users").document(auth.currentUser!!.uid).update("groups",FieldValue.arrayUnion(groupID)).await()
                            }
                            catch (e: Exception){
                                message += "ERROR: ${e.message!!.split(".")[0]}\n"
                            }
                        }
                        launch {
                            try{
                                db.collection("groups").document(groupID).update("groups",FieldValue.arrayUnion(groupID)).await()
                            }
                            catch (e: Exception){
                                message += "ERROR: ${e.message!!.split(".")[0]}\n"
                            }
                        }
                    }
                    else{
                        message = "ERROR: Group does not exist!"
                    }
                }
            }
        return message

    }
    fun loadGroups() : HashMap<String,FMFGroup> {

        val groups = HashMap<String,FMFGroup>()
        Log.d("GROUPSloading","GROUPSloading")

        runBlocking {
            launch{
                val groupIDs = db.collection("users").document(auth.currentUser!!.uid).get().await().data?.get("groups")
                if (groupIDs!= null){
                    for (id in groupIDs as List<String>) {
                        launch{
                            val group = db.collection("groups").document(id).get().await().toObject<FMFGroup>()
                            if (group != null) {
                                groups[id] = group
                            }
                        }

                    }
                }

            }
        }
        Log.d("GROUPSloaded",groups.toString())
        return groups
    }
    fun loadGroup(groupID: String) : FMFGroup {
        var group = FMFGroup()
        runBlocking {
            launch{
                Log.d("LOADEDGROUPTAGID", groupID.toString())

                val groupRef = db.collection("groups")
                    val groupRef2= groupRef.document(groupID).get().await()
                if (groupRef2 != null){
                    group = groupRef2.toObject<FMFGroup>()!!
                }
                Log.d("LOADEDGROUPTAG", groupRef.toString())

            }
        }
        return group
    }


    fun loadGroupUsers(groupID: String): HashMap<String,FMFUser>{
        var users = HashMap<String,FMFUser>()
        runBlocking {
            launch{
                val group = db.collection("groups").document(groupID).get().await().toObject<FMFGroup>()
                if (group?.userIDs!= null){
                    for (id in group?.userIDs!!) {
                        launch{
                            val user = db.collection("users").document(id).get().await()
                            if (user.exists()) {

                                Log.d("USERTAG", user.data.toString())
                                users[id] = (FMFUser(user.data?.get("name").toString(),"",id))
                            }
                        }

                    }
                }

            }
        }
        Log.d("USERTAGLoaded",users.toString())
        return users
    }

    fun logout(){
        auth.signOut()
        currentUser = null

    }
}