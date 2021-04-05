package com.developeryatin.peopledatafetchingdemo.model

import com.google.gson.JsonArray

data class PeopleDataModel(
    var data: JsonArray?
)

data class PeoplesData(
    val id: String,
    val name: String,
    val email: String,
    val status: String,
    val created_at: String,
)
