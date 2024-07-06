package com.example.offbeat.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object{
        private const  val PREF_NAME = "user_prefs"
        private const  val NAME = "name"
        private const  val EMAIL = "email"
    }

    fun saveUser(name: String, email: String){
        editor.putString(NAME, name)
        editor.putString(EMAIL, email)
        editor.apply()
    }

    fun getUserName():String?{
        return sharedPreferences.getString(NAME,null)
    }

    fun getUserEmail(): String?{
        return sharedPreferences.getString(EMAIL, null)
    }

    fun clearuser(){
        editor.clear()
        editor.apply()
    }
}