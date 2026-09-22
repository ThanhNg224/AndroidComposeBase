package com.thanhng224.androidcomposebase.core.navigation

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import androidx.core.os.BundleCompat as AndroidXBundleCompat

/**
 * Type-safe reified factory to extract non-null extras from an Activity Intent.
 */
public inline fun <reified T> intentExtra(
    key: String,
    defaultValue: T? = null,
): ReadOnlyProperty<Activity, T> = IntentExtraDelegate(key, T::class.java, defaultValue)

/**
 * Type-safe reified factory to extract nullable extras from an Activity Intent.
 */
public inline fun <reified T> intentExtraNullable(key: String): ReadOnlyProperty<Activity, T?> =
    IntentExtraNullableDelegate(key, T::class.java)

@PublishedApi
internal class IntentExtraDelegate<T>(
    private val key: String,
    private val clazz: Class<T>,
    private val defaultValue: T? = null,
) : ReadOnlyProperty<Activity, T> {
    override fun getValue(
        thisRef: Activity,
        property: KProperty<*>,
    ): T {
        val bundle = thisRef.intent?.extras
        val value = bundle?.getTyped(key, clazz)
        return value ?: defaultValue ?: throw IllegalArgumentException(
            "Intent extra with key '$key' is missing or has incorrect type.",
        )
    }
}

@PublishedApi
internal class IntentExtraNullableDelegate<T>(
    private val key: String,
    private val clazz: Class<T>,
) : ReadOnlyProperty<Activity, T?> {
    override fun getValue(
        thisRef: Activity,
        property: KProperty<*>,
    ): T? = thisRef.intent?.extras?.getTyped(key, clazz)
}

/**
 * Extension helper to safely fetch typed attributes from bundles using non-deprecated APIs.
 *
 * Every type Bundle can legitimately hold has a dedicated type-safe getter, so there is no
 * generic fallback here: an unsupported [clazz] is a programmer error, not a runtime null.
 */
@Suppress("UNCHECKED_CAST", "ComplexMethod")
public fun <T> Bundle.getTyped(
    key: String,
    clazz: Class<T>,
): T? =
    when {
        clazz == String::class.java -> getString(key) as? T
        clazz == CharSequence::class.java -> getCharSequence(key) as? T
        clazz == Int::class.java -> {
            if (containsKey(key)) getInt(key) as? T else null
        }
        clazz == Boolean::class.java -> {
            if (containsKey(key)) getBoolean(key) as? T else null
        }
        clazz == Long::class.java -> {
            if (containsKey(key)) getLong(key) as? T else null
        }
        clazz == Float::class.java -> {
            if (containsKey(key)) getFloat(key) as? T else null
        }
        clazz == Double::class.java -> {
            if (containsKey(key)) getDouble(key) as? T else null
        }
        clazz == Byte::class.java -> {
            if (containsKey(key)) getByte(key) as? T else null
        }
        clazz == Short::class.java -> {
            if (containsKey(key)) getShort(key) as? T else null
        }
        clazz == Char::class.java -> {
            if (containsKey(key)) getChar(key) as? T else null
        }
        clazz == Bundle::class.java -> getBundle(key) as? T
        Parcelable::class.java.isAssignableFrom(clazz) -> {
            AndroidXBundleCompat.getParcelable(this, key, clazz as Class<out Parcelable>) as? T
        }
        Serializable::class.java.isAssignableFrom(clazz) -> {
            AndroidXBundleCompat.getSerializable(this, key, clazz as Class<out Serializable>) as? T
        }
        else -> throw IllegalArgumentException("Unsupported bundle value type: ${clazz.name}")
    }
