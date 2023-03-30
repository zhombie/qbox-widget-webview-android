inline fun <reified T : Enum<T>> findEnumBy(predicate: (T) -> Boolean): T? =
    T::class.java.enumConstants?.find(predicate)

inline fun <reified T : Enum<T>> findEnumByName(name: String): T? =
    findEnumBy<T> { it.name == name }
