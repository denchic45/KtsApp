package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.mapper.Default
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import com.denchic45.kts.util.UUIDS

data class SimpleEventDetails @Default constructor (
    override var id: String,
    val name: String,
    val iconUrl: String,
    val color: String
) : EventDetails() {

    private constructor() : this("", "", "","")

    constructor(name: String):this (
        UUIDS.createShort(), name,
            "https://firebasestorage.googleapis.com/v0/b/kts-app-2ab1f.appspot.com/o/subjects%2Fevent.svg?alt=media&token=082f0838-b01b-4a51-9c7a-f111380c5689",
        "blue"
    )

    override fun copy(): SimpleEventDetails {
        return SimpleEventDetails(id, name, iconUrl, color)
    }

    override val type: TYPE
        get() = TYPE.SIMPLE

    companion object {

        fun dinner(): SimpleEventDetails {
            return SimpleEventDetails(
                UUIDS.createShort(),
                "Обед",
                "https://firebasestorage.googleapis.com/v0/b/kts-app-2ab1f.appspot.com/o/subjects%2Flunch.svg?alt=media&token=e2d320ae-8fca-4533-aa4b-693bc3260721",
                "yellow"
            )
        }

        fun practice(): SimpleEventDetails {
            return SimpleEventDetails(
                UUIDS.createShort(),
                "Практика",
                "https://firebasestorage.googleapis.com/v0/b/kts-app-2ab1f.appspot.com/o/subjects%2Fpractice.svg?alt=media&token=f20fc51a-3374-4815-9a08-1d206957203d",
                "blue"
            )
        }

        fun createEmpty() = SimpleEventDetails()
    }
}