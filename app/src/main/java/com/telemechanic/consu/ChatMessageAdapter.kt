package com.telemechanic.consu

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.chat.models.TextMessage
import com.google.gson.Gson
import com.telemechanic.consu.databinding.ItemReceiverBinding
import com.telemechanic.consu.databinding.ItemSenderBinding
import com.telemechanic.consu.datamodel.ChatMessageData
import com.telemechanic.consu.datamodel.RawMessageData
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class ChatMessageAdapter(var context: Context,var uId:String,var chatClickListeners:ChatClicks) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val mChatClickInterface: ChatClicks? = null
    val messagesList = mutableListOf<ChatMessageData>()
    private var highlightThresholdId: Int? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val binding = ItemSenderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SenderViewHolder(binding)
        } else {
            val binding = ItemReceiverBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceiverViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        if (holder is SenderViewHolder) {
            holder.bind(message, position)
        } else if (holder is ReceiverViewHolder) {
            holder.bind(message)
        }
    }


    inner class SenderViewHolder(private val binding: ItemSenderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessageData, pos: Int) {
            binding.apply {
                llReceiverMsgText.visibility = View.GONE
                llReceiverMsgImg.visibility = View.GONE
                llReceiverMsgAttachment.visibility = View.GONE

                if (chatMessage.type == "text") {
                    llReceiverMsgText.visibility = View.VISIBLE
                    tvTextMsg.text = chatMessage.text

                } else if (chatMessage.type == "image") {
                    llReceiverMsgImg.visibility = View.VISIBLE
                    val imageUrl = chatMessage.attachment?.fileUrl
                    if (imageUrl != null) {
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_chat_user_new)
                            .into(ivImage)
                    }
                } else if (chatMessage.type == "video") {
                    llReceiverMsgImg.visibility = View.VISIBLE
                    val videoUrl = chatMessage.attachment?.fileUrl
                    if (videoUrl != null) {
                        Glide.with(context)
                            .load(videoUrl)
                            .placeholder(R.drawable.img_chat_user_new)
                            .into(ivImage)
                    }
                } else if (chatMessage.type == "file") {
                    llReceiverMsgAttachment.visibility = View.VISIBLE
                    Log.d("getAttachmentData", chatMessage.toString())
                    Log.d("getAttachment", chatMessage.attachment.toString())
                    tvSReceiverTextAttachmentName.text = chatMessage.attachment?.fileName
                    tvReceiverTextAttachmentSize.text =
                        formatFileSize(chatMessage.attachment?.fileSize!!.toLong()).toString()
                }

                binding.tvReceiverMsgTime.text = formatTimestampToTime(chatMessage.sentAt!!)

                if (highlightThresholdId != null && pos <= highlightThresholdId!!) {
                    tvReceiverMsgTime.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_tick,
                        0,
                        0,
                        0
                    )
                } else {
                    tvReceiverMsgTime.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_grey_tick,
                        0,
                        0,
                        0
                    )
                }
                ivReceiverImageAttachmentDownload.setOnClickListener {
                    downloadFile(
                        context,
                        chatMessage.attachment?.fileUrl!!.toString(),
                        chatMessage.attachment?.fileName.toString()
                    )
                }
                llReceiverMsgText.setOnLongClickListener {
                    Log.d("getDeleteClick!", "true")
                    chatClickListeners.deleteMsgClick(chatMessage.id!!, pos)
                    return@setOnLongClickListener true
                }
            }
        }

    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading $fileName")
        request.setDescription("File is downloading...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setAllowedOverMetered(true) // Allow downloads over mobile data
        request.setAllowedOverRoaming(false) // Disallow downloads while roaming
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val base = 1024.0
        val exponent = (Math.log(bytes.toDouble()) / Math.log(base)).toInt()
        val value = bytes / Math.pow(base, exponent.toDouble())

        return String.format("%.1f %s", value, units[exponent])
    }

//    fun getImageUrlFromRawMessage(chatMessage: ChatMessageData): String? {
//        return try {
//            val gson = Gson()
//            val rawMessage = chatMessage.rawMessage as? RawMessageData
//            rawMessage?.attachments!![0].url
//        } catch (e: Exception) {
//            Log.e("ImageUrlError", "Error extracting URL with Gson: ${e.message}")
//            null
//        }
//    }
        inner class ReceiverViewHolder(private val binding: ItemReceiverBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(chatMessage: ChatMessageData) {
                binding.apply {
                    llSenderMsgText.visibility = View.GONE
                    llSenderMsgImg.visibility = View.GONE
                    llSenderMsgAttachment.visibility = View.GONE

                    if (chatMessage.type == "text") {
                        llSenderMsgText.visibility = View.VISIBLE
                        tvTextMsg.text = chatMessage.text
                    } else if (chatMessage.type == "image") {
                        llSenderMsgImg.visibility = View.VISIBLE
                        val imageUrl = chatMessage.attachment?.fileUrl
                        if (imageUrl != null) {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.img_chat_user_new)
                                .into(ivImage)
                        }
                    } else if (chatMessage.type == "video") {
                        llSenderMsgImg.visibility = View.VISIBLE
                        val videoUrl = chatMessage.attachment?.fileUrl
                        if (videoUrl != null) {
                            Glide.with(context)
                                .load(videoUrl)
                                .placeholder(R.drawable.img_chat_user_new)
                                .into(ivImage)
                        }
                    } else if (chatMessage.type == "file") {
                        llSenderMsgAttachment.visibility = View.VISIBLE
                        Log.d("getAttachmentData", chatMessage.toString())
                        Log.d("getAttachment", chatMessage.attachment.toString())
                        tvSenderTextAttachmentName.text = chatMessage.attachment?.fileName
                        tvSenderTextAttachmentSize.text =
                            formatFileSize(chatMessage.attachment?.fileSize!!.toLong()).toString()
                    }

                    ivImageAttachmentDownload.setOnClickListener {
                        downloadFile(
                            context,
                            chatMessage.attachment?.fileUrl!!.toString(),
                            chatMessage.attachment?.fileName.toString()
                        )
                    }
                }
            }
        }

        override fun getItemCount(): Int = messagesList.size

        override fun getItemViewType(position: Int): Int {
            return if (messagesList[position].senderUid == uId) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
        }

        fun addMessage(newMessage: ChatMessageData?) {
            if (newMessage != null) {
                messagesList.add(newMessage)
                notifyItemInserted(messagesList.size - 1) // Notify the adapter about the new item
            }
        }
    fun deleteMessage(messageId: Int?) {
                var index= messagesList.indexOfFirst { it.id == messageId }
                messagesList.removeAt(index)
        notifyDataSetChanged() // Notify the adapter about the new item
            }


        fun readMessage(messageId: Int) {
            highlightThresholdId = messagesList.indexOfFirst { it.id == messageId }
            notifyDataSetChanged()
        }


        fun formatTimestampToTime(timestamp: Long): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use java.time (API 26+)
                val instant = Instant.ofEpochSecond(timestamp)
                val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                formatter.withZone(ZoneId.systemDefault()).format(instant).uppercase()
            } else {
                // Use SimpleDateFormat for pre-API 26
                val date = Date(timestamp * 1000) // Convert seconds to milliseconds
                val format = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                format.format(date).uppercase()
            }
        }

        companion object {
            private const val VIEW_TYPE_SENDER = 0
            private const val VIEW_TYPE_RECEIVER = 1
        }

    interface ChatClicks{
        fun deleteMsgClick(messageId:Int,pos:Int)
    }
}


