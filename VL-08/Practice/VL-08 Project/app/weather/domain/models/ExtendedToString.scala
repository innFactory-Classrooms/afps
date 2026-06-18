package weather.domain.models

trait ExtendedToString { p: Product => 
  override def toString: String = {
    val attributeNames = this.productElementNames
    val attributeValues = this.productIterator
    attributeNames
      .zip(attributeValues)
      .map((str, value) => s"$str: $value")
      .mkString(s"${this.getClass.getSimpleName}(", ", ", ")")
  }
}
