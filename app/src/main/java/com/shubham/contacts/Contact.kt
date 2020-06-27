package com.shubham.contacts

data class Contact (val contactId:Long,
                    val name:String,
                    val phone:String?,
                    val photo:String? = null,
                    var selected:Boolean = false)