package com.example.pochtao.controllers

import com.example.pochtao.AppExecutors
import com.example.pochtao.beans.Credentials
import com.example.pochtao.beans.PaginatedEmails
import com.example.pochtao.enums.Mailboxes
import com.sun.mail.imap.IMAPSSLStore
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.inject.Singleton
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.math.ceil

@Singleton
class EmailController {
    lateinit var appExecutors: AppExecutors
    private val props: Properties = System.getProperties()

    private val session: Session

    init {
        props["mail.smtp.host"] = "mx.paul156551.live"
        props["mail.imaps.host"] = "mx.paul156551.live"
        props["mail.smtp.socketFactory.port"] = "587"
        props["mail.smtp.socketFactory.class"] =
            "com.example.Pochtao.beans.AlwaysTrustSSLContextFactory"
        props["mail.imaps.socketFactory.port"] = "993"
        props["mail.imaps.socketFactory.class"] =
            "com.example.Pochtao.beans.AlwaysTrustSSLContextFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.imaps.auth"] = "true"
        props["mail.smtp.port"] = "587"
        props["mail.imaps.port"] = "993"
        props["mail.smtp.ssl.trust"] = "*"
        props["mail.imaps.ssl.trust"] = "*"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.imaps.starttls.enable"] = "true"

        session = Session.getDefaultInstance(props,
            object : Authenticator() {
                //Authenticating the password
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(Credentials.EMAIL, Credentials.PASSWORD)
                }
            })
    }

    // send email
    // The email argument is the email to send to
    // The subject argument is the subject of the email
    // The content argument is the content of the email
    // The attachments argument is the attachments of the email
    fun sendEmail(email: String, subject: String, content: String, attachment: String = "") {
        println("Sending email to $email with subject $subject and content $content ${if (attachment.isNotEmpty()) " and attachment $attachment" else ""}")
        appExecutors.diskIO().execute { // Execute a disk thread
            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(Credentials.EMAIL))
                message.setRecipients(Message.RecipientType.TO, email)
                message.subject = subject
                message.setText(content)

                if (attachment.isNotEmpty()) {
                    val attachmentFile = FileDataSource(attachment)
                    val attachmentPart = MimeBodyPart()
                    attachmentPart.dataHandler = DataHandler(attachmentFile)
                    attachmentPart.fileName = attachmentFile.name
                    val multipart = MimeMultipart()
                    multipart.addBodyPart(attachmentPart)
                    message.setContent(multipart)
                }

                appExecutors.networkIO().execute { // Execute a network thread
                    Transport.send(message)
                }

                appExecutors.networkIO().execute { // Execute a network thread
                    val store = IMAPSSLStore(
                        session,
                        URLName(
                            "imaps://${
                                URLEncoder.encode(
                                    Credentials.EMAIL,
                                    "UTF-8"
                                )
                            }:${Credentials.PASSWORD}@mx.gregoire.live:993"
                        )
                    )  // Define the IMAPs store
                    store.connect() // Connect to the IMAPs store
                    val folder = store.getFolder(Mailboxes.SENT.value) // Get the sent folder
                    folder.open(Folder.READ_WRITE) // Open the sent folder in read/write mode
                    folder.appendMessages(arrayOf(message))
                    message.setFlag(Flags.Flag.RECENT, true)
                    message.setFlag(Flags.Flag.SEEN, true)
                    message.saveChanges()
                    folder.close() // Close the folder
                    store.close() // Close the store
                }

                appExecutors.mainThread().execute { // Execute a main thread
                    println("Email sent successfully and saved to Sent folder")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Retrieve all emails from a mailbox and pass them to the callback function
    // The mailbox argument can be one of the following: INBOX, SENT, DRAFTS, TRASH
    fun retrieveAllEmails(mailbox: Mailboxes, callback: (Array<Message>) -> Unit) {
        var messages: Array<Message>
        val networkThread = appExecutors.networkIO()
        val mainThread = appExecutors.mainThread()
        val diskThread = appExecutors.diskIO()

        diskThread.execute { // Execute a disk thread
            try {

                networkThread.execute { // Execute a network thread
                    val store = IMAPSSLStore(
                        session,
                        URLName(
                            "imaps://${
                                URLEncoder.encode(
                                    Credentials.EMAIL,
                                    "UTF-8"
                                )
                            }:${Credentials.PASSWORD}@mx.gregoire.live:993"
                        )
                    )
                    store.connect()
                    val folder = store.getFolder(mailbox.value)
                    folder.open(Folder.READ_ONLY)
                    messages = folder.messages
                    mainThread.execute {
                        println("Retrieved ${messages.size} emails")
                    }
                    callback.invoke(messages)
                    folder.close()
                    store.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Retrieve a paginated list of emails from a mailbox and pass them to the callback function in a PaginatedEmails bean
    // The mailbox argument can be one of the following: INBOX, SENT, DRAFTS, TRASH
    // The page argument is the page number to retrieve
    // The itemsPerPage argument is the number of emails to retrieve per page
    // The callback function will be called with a PaginatedEmails bean containing the list of emails and the total number of pages, etc.
    fun retrievePaginatedEmails(
        mailbox: Mailboxes,
        page: Int,
        itemsPerPage: Int = 10,
        callback: (PaginatedEmails) -> Unit
    ) {
        var messages: Array<Message>
        var firstMessageOnRequestedPage: Int
        var lastMessageOnRequestedPage: Int
        val networkThread = appExecutors.networkIO()
        val mainThread = appExecutors.mainThread()
        val diskThread = appExecutors.diskIO()

        diskThread.execute { // Execute a disk thread
            try {
                networkThread.execute { // Execute a network thread
                    val store = IMAPSSLStore(
                        session,
                        URLName(
                            "imaps://${
                                URLEncoder.encode(
                                    Credentials.EMAIL,
                                    "UTF-8"
                                )
                            }:${Credentials.PASSWORD}@mx.gregoire.live:993"
                        )
                    )
                    store.connect()
                    val folder = store.getFolder(mailbox.value)
                    folder.open(Folder.READ_ONLY)
                    val totalPages = ceil(folder.messageCount / itemsPerPage.toDouble()).toInt()
                    val paginatedEmails =
                        PaginatedEmails(page = page + 1, totalPages = totalPages, itemsPerPage = itemsPerPage)
                    firstMessageOnRequestedPage = page * itemsPerPage + 1
                    lastMessageOnRequestedPage = firstMessageOnRequestedPage + itemsPerPage - 1
                    messages = if (firstMessageOnRequestedPage > folder.messageCount) {
                        arrayOf()
                    } else {
                        if (lastMessageOnRequestedPage > folder.messageCount) {
                            folder.getMessages(firstMessageOnRequestedPage, folder.messageCount).reversedArray()
                        } else {
                            folder.getMessages(firstMessageOnRequestedPage, lastMessageOnRequestedPage).reversedArray()
                        }
                    }
                    paginatedEmails.emails = messages
                    mainThread.execute {
                        println("Retrieved ${messages.size} emails")
                    }
                    callback.invoke(paginatedEmails)
                    folder.close()
                    store.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Get the number of emails in a mailbox with or without a notSeen filter and pass it to the callback function
    // The mailbox argument can be one of the following: INBOX, SENT, DRAFTS, TRASH
    // The notSeen argument is a boolean to filter the number of emails with or without a notSeen flag
    // The callback function will be called with the number of emails
    fun getEmailsCount(mailbox: Mailboxes, notSeen: Boolean = false, callback: (Int) -> Unit) {
        appExecutors.diskIO().execute { // Execute a disk thread
            try {
                appExecutors.networkIO().execute { // Execute a network thread
                    val store = IMAPSSLStore(
                        session,
                        URLName(
                            "imaps://${
                                URLEncoder.encode(
                                    Credentials.EMAIL,
                                    "UTF-8"
                                )
                            }:${Credentials.PASSWORD}@mx.gregoire.live:993"
                        )
                    )
                    store.connect()
                    val folder = store.getFolder(mailbox.value)
                    folder.open(Folder.READ_ONLY)
                    if(notSeen) {
                        callback.invoke(folder.unreadMessageCount)
                    } else {
                        callback.invoke(folder.messageCount)
                    }
                    folder.close()
                    store.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Set seen status of an email and pass result status to the callback function
    // The mailbox argument can be one of the following: INBOX, SENT, DRAFTS, TRASH
    // The message argument is the email to set the seen status
    // The status argument is a boolean to set the seen status to true or false
    // The callback function will be called with the result status
    fun setSeen(mailbox: Mailboxes, message: Message, status: Boolean, callback: ((Boolean) -> Unit)?) {
        val mainThread = appExecutors.mainThread()

        appExecutors.diskIO().execute { // Execute a disk thread
            try {
                appExecutors.networkIO().execute { // Execute a network thread
                    val store = IMAPSSLStore(
                        session,
                        URLName(
                            "imaps://${
                                URLEncoder.encode(
                                    Credentials.EMAIL,
                                    "UTF-8"
                                )
                            }:${Credentials.PASSWORD}@mx.gregoire.live:993"
                        )
                    )
                    store.connect()
                    val folder = store.getFolder(mailbox.value)
                    folder.open(Folder.READ_WRITE)
                    val modifiedMessage = folder.getMessage(message.messageNumber)
                    modifiedMessage.setFlag(Flags.Flag.SEEN, status)
                    modifiedMessage.saveChanges()
                    folder.close()
                    store.close()
                    mainThread.execute {
                        callback?.invoke(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mainThread.execute {
                    callback?.invoke(false)
                }
            }
        }
    }


    // Get text content of an email
    // The message argument is the email to get the text content
    @Throws(MessagingException::class, IOException::class)
    fun getTextFromMessage(message: Message): String {
        var result = ""
        if (message.isMimeType("text/plain")) {
            result = message.content.toString()
        } else if (message.isMimeType("text/html")) { // **
            result = message.content.toString() // **
        } else if (message.isMimeType("multipart/*")) {
            val mimeMultipart = message.content as MimeMultipart
            result = getTextFromMimeMultipart(mimeMultipart)
        }
        return result
    }

    // Get text content of a multipart email
    // The mimeMultipart argument is the multipart email to get the text content
    @Throws(MessagingException::class, IOException::class)
    fun getTextFromMimeMultipart(
        mimeMultipart: MimeMultipart
    ): String {
        var result = ""
        val count = mimeMultipart.count
        for (i in 0 until count) {
            val bodyPart = mimeMultipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result = """
                $result
                ${bodyPart.content}
                """.trimIndent()
                break // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                val html = bodyPart.content as String
                result = """
                $result
                ${org.jsoup.Jsoup.parse(html).text()}
                """.trimIndent()
            } else if (bodyPart.content is MimeMultipart) {
                result += getTextFromMimeMultipart(bodyPart.content as MimeMultipart)
            }
        }
        return result
    }
}
