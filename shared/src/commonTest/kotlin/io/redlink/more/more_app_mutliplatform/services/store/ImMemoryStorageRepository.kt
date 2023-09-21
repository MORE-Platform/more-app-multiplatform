package io.redlink.more.more_app_mutliplatform.services.store

class ImMemoryStorageRepository : SharedStorageRepository {

    private val storageMap = HashMap<String, Any>()
    override fun store(key: String, value: List<String>) {
        storageMap[key] = value
    }

    override fun store(key: String, value: String) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Boolean) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Int) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Float) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Double) {
        storageMap[key] = value
    }

    override fun load(key: String, default: List<String>): List<String> {
        return storageMap[key] as? List<String> ?: default

    }

    override fun load(key: String, default: String): String {
        return storageMap[key] as? String ?: default
    }

    override fun load(key: String, default: Boolean): Boolean {
        return storageMap[key] as? Boolean ?: default
    }

    override fun load(key: String, default: Int): Int {
        return storageMap[key] as? Int ?: default
    }

    override fun load(key: String, default: Float): Float {
        return storageMap[key] as? Float ?: default
    }

    override fun load(key: String, default: Double): Double {
        return storageMap[key] as? Double ?: default
    }

    override fun remove(key: String) {
        storageMap.remove(key)
    }
}