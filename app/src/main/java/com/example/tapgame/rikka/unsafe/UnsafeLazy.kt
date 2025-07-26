// Файл: com/example/tapgame/rikka/unsafe/UnsafeLazy.kt
package com.example.tapgame.rikka.unsafe

import kotlin.reflect.KProperty

class UnsafeLazyImpl<T>(initializer: () -> T) : Lazy<T> {
    private var initializer: (() -> T)? = initializer
    private var _value: Any? = UNINITIALIZED_VALUE

    override val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer!!()
                initializer = null
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

private object UNINITIALIZED_VALUE

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = UnsafeLazyImpl(initializer)
