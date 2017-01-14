package info.ganczarek.model

abstract class ItemRate {
  def title: String
  def year: Int
  def rate: Int
}
case class MovieRate(title: String, year: Int, rate: Int) extends ItemRate
case class SeriesRate(title: String, year: Int, rate: Int) extends ItemRate
