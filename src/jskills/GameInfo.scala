package jskills

object GameInfo {
  private val defaultInitialMean = 25.0
  private val defaultBeta = defaultInitialMean / 6.0
  private val defaultDrawProbability = 0.10
  val defaultDynamicsFactor = defaultInitialMean / 300.0
  private val defaultInitialStandardDeviation = defaultInitialMean / 3.0

  def getDefaultGameInfo(): GameInfo = {
    // We  a fresh copy since we have public setters that can mutate state
    new GameInfo(defaultInitialMean,
      defaultInitialStandardDeviation,
      defaultBeta,
      defaultDynamicsFactor,
      defaultDrawProbability)
  }

}
/**
 * Parameters about the game for calculating the TrueSkill.
 */
class GameInfo(
  private val initialMean: Double,
  private val initialStandardDeviation: Double,
  val beta: Double,
  val dynamicsFactor: Double,
  val drawProbability: Double) {

  def getDefaultRating(): Rating = {
    new Rating(initialMean, initialStandardDeviation)
  }
}