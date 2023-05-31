package com.zhouchengang.chatglmsample

import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  @author zhouchengang
 *  @date   2021/2/8
 *  @desc
 */
class ChatAdapter : BaseMultiItemQuickAdapter<ChatListData, BaseViewHolder> {
    companion object {
        private const val TAG = "ChatAdapter"
    }

    constructor() {
        addItemType(ChatListData.MOD_ME, R.layout.item_chat_me)
        addItemType(ChatListData.MOD_BOT, R.layout.item_chat_bot)
    }

    override fun convert(holder: BaseViewHolder, item: ChatListData) {
        Log.d(TAG, "convert: PicGridAdapter")
        holder.getView<TextView>(R.id.tv_chat).text = item.message
        Glide.with(context)
            .load(if (item.isBOT()) R.drawable.ic_kokomi else R.drawable.ic_chat_me)
            .apply(
                RequestOptions()
                    .transform(GlideRoundTransformCenterCrop(30f))
                    .dontAnimate()
            )
            .into(holder.getView(R.id.iv_head))

        holder.itemView.setOnClickListener { view ->

        }
    }

    override fun convert(holder: BaseViewHolder, item: ChatListData, payloads: List<Any>) {
        if (payloads != null && payloads.contains("STREAM")) {
            holder.getView<TextView>(R.id.tv_chat).text = item.message
        } else {
            convert(holder, item)
        }
    }
}

class ChatListData(override var itemType: Int, var message: String, var isLoad: Boolean = false) :
    MultiItemEntity {
    companion object {
        const val MOD_ME = 0
        const val MOD_BOT = 1
    }

    fun isBOT() = (MOD_BOT == itemType)
}