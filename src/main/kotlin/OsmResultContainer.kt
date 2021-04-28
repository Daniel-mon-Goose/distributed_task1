class OsmResultContainer {
    val users: MutableMap<String, Int> = HashMap()
    val tags: MutableMap<String, Int> = HashMap()

    fun userEditsCount(user: String) {
        users.merge(user, 1, Int::plus)
    }

    fun incrementTagCount(tag: String) {
        tags.merge(tag, 1, Int::plus)
    }
}
