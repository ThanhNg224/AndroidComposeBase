# Consumer ProGuard/R8 rules for apps that depend on :core with minification enabled.
# Hilt, Retrofit, OkHttp, kotlinx.serialization and Compose all ship their own consumer rules inside
# their artifacts; this file only covers :core's own classes that a library reflects into by name.
#
# There are currently none. The previous `-keep` on core.storage.database.** existed for Room, and
# :core does not ship a database — a consumer declares its own
# `@Database`, so that keep rule belongs in their build, not here.
#
# The local publication consumer gate builds both artifacts and verifies the consuming app with
# minification enabled. Add rules here only when reflection in :core requires them.

# Keep generic signature and class structure metadata if needed for reflection.
-keepattributes Signature, InnerClasses, EnclosingMethod
