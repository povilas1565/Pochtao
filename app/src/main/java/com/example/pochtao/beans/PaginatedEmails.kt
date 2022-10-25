package com.example.pochtao.beans

import javax.mail.Message

class PaginatedEmails(
    var emails: Array<Message> = arrayOf(),
    page: Int,
    totalPages: Int,
    itemsPerPage: Int = 10,
    hasNextPage: Boolean = page < totalPages,
    hasPreviousPage: Boolean = page > 1
) {

    var page: Int = page
        private set

    var totalPages: Int = totalPages
        private set

    var itemsPerPage: Int = itemsPerPage
        private set

    var hasNextPage: Boolean = hasNextPage
        private set

    var hasPreviousPage: Boolean = hasPreviousPage
        private set


}