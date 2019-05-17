package database.operations

import database.Book

final class FindResult(val book: Option[Book])  extends Serializable
