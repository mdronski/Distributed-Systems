package database.operations

import database.Book

final case class FindResult(book: Option[Book])