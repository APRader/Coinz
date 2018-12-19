package com.example.s1611382.ilp

import android.os.Parcel
import android.os.Parcelable

// it's Parcelable so we can pass it to different activities
data class Coin(val id: String, val value: Float, val currency: String, val traded: Int = 0) : Parcelable {
    constructor(parcel: Parcel) : this(
            // if null, return empty string
            parcel.readString() ?: "",
            parcel.readFloat(),
            parcel.readString() ?: "",
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeFloat(value)
        parcel.writeString(currency)
        parcel.writeInt(traded)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        var isTraded = ""
        if (traded == 1) {
            isTraded = ", traded"
        }
        return " %.2f $currency$isTraded".format(value)
    }

    companion object CREATOR : Parcelable.Creator<Coin> {
        override fun createFromParcel(parcel: Parcel): Coin {
            return Coin(parcel)
        }

        override fun newArray(size: Int): Array<Coin?> {
            return arrayOfNulls(size)
        }
    }
}