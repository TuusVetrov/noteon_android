package com.sophimp.are.listener

interface IOssServer {

    fun isServerPath(path: String?): Boolean
    fun getMemoAndDiaryImageUrl(url: String?): String

    fun obtainOssPrefixByType(type: String): String

}