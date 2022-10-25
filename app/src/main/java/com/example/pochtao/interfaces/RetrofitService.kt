package com.example.pochtao.interfaces

import com.example.pochtao.beans.MailboxBean
import com.example.pochtao.beans.RetrofitResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface  RetrofitService {
    @POST("/verify-mailbox")
    suspend fun verifyMailbox(@Body  mailboxBean: MailboxBean): RetrofitResponse

    @POST("/create-mailbox")
    suspend fun createMailbox(@Body mailboxBean: MailboxBean): RetrofitResponse

    @POST("/modify-mailbox")
    suspend fun modifyMailbox(@Body mailboxBean: MailboxBean): RetrofitResponse

    @GET("/get-mailbox")
    suspend fun getMailbox(@Body mailboxBean: MailboxBean) : RetrofitResponse
}