package models


case class City(
                 name: String,
                 lat: Double,
                 lon: Double
               ) extends ExtendedToString