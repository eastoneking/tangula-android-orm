package com.tangula.utils

/*
 * will remove when togula-commons version upgrounded
 */
class ConcurrentUtils {

    companion object {

        fun doubleCheckRun(lock:Any, predicate:()->Boolean, task:(()->Unit)?){
            if(predicate()){
                synchronized(lock){
                    if(predicate()){
                        task?.apply { this() }
                    }
                }
            }
        }

    }

}