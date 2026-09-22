package com.thanhng224.androidcomposebase.core.storage.secure

import android.content.Context
import com.thanhng224.androidcomposebase.core.foundation.AppDispatchers
import com.thanhng224.androidcomposebase.core.foundation.SecureStore

public object SecureStoreFactory {
    public fun encrypted(
        context: Context,
        dispatchers: AppDispatchers,
    ): SecureStore = EncryptedFileSecureStore(context.applicationContext, dispatchers)
}
