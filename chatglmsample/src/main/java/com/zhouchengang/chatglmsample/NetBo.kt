package com.zhouchengang.chatglmsample

import java.io.Serializable

open class BaseBean() : Serializable {

//    var status: Int = 0
//    var time: String? = null
    var response: String? = null
    var history: ArrayList<ArrayList<String?>?>? = null


    companion object{
        fun transHistoryRequest(  history: ArrayList<ArrayList<String?>?>? ): String {
            if (history == null)
                return "[]"
            var ret = "["
            for ((index, item) in history!!.withIndex()) {
                if (index != 0)
                    ret += ","
                if (item == null)
                    ret += "[]"
                ret += "["
                for ((index, item2) in item!!.withIndex()) {
                    if (index != 0)
                        ret += ","
                    ret += "\""
                    ret += item2
                    ret += "\""
                }
                ret += "]"
            }
            ret += "]"
            return ret
        }
    }

}


