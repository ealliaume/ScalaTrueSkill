package jskills.elo;

import java.util.Collection
import java.util.EnumSet
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Map.Entry
import jskills.GameInfo
import jskills.IPlayer
import jskills.ITeam
import jskills.PairwiseComparison
import jskills.Player
import jskills.RankSorter
import jskills.Rating
import jskills.SkillCalculator
import jskills.Team
import jskills.numerics.MathUtils
import jskills.numerics.Range;
import jskills.SkillCalculator.SupportedOptions
import collection.JavaConversions._

class DuellingEloCalculator(twoPlayerEloCalculator: TwoPlayerEloCalculator)
  extends SkillCalculator(EnumSet.noneOf(classOf[SupportedOptions]), Range.atLeast(2), Range
    .atLeast(1)) {

  override def calculateNewRatings(gameInfo: GameInfo,
    teams: Collection[ITeam], teamRanks: Int*): Map[IPlayer, Rating] = {
    // On page 6 of the TrueSkill paper, the authors write:
    /*
		 * "When we had to process a team game or a game with more than two
		 * teams we used the so-called *duelling* heuristic: For each player,
		 * compute the ?'s in comparison to all other players based on the team
		 * outcome of the player and every other player and perform an update
		 * with the average of the ?'s."
		 */
    // This implements that algorithm.

    validateTeamCountAndPlayersCountPerTeam(teams);
    val tr = teamRanks.toList.toArray[Int]
    val teamsl = RankSorter.sort(teams, tr);
    val teamsList = teamsl.toArray(new Array[ITeam](0));

    val deltas = new HashMap[IPlayer, Map[IPlayer, Double]]();

    for (ixCurrentTeam <- 0 until teamsList.length) {
      for (ixOtherTeam <- 0 until teamsList.length) {
        if (ixOtherTeam != ixCurrentTeam) {

          val currentTeam = teamsList(ixCurrentTeam);
          val otherTeam = teamsList(ixOtherTeam);

          // Remember that bigger numbers mean worse rank (e.g.
          // other-current is what we want)
          val comparison = PairwiseComparison
            .fromMultiplier(Math
              .signum(tr(ixOtherTeam)
                - tr(ixCurrentTeam)));

          for (
            currentTeamPlayerRatingPair <- currentTeam
              .entrySet()
          ) {
            for (
              otherTeamPlayerRatingPair <- otherTeam
                .entrySet()
            ) {
              updateDuels(gameInfo, deltas,
                currentTeamPlayerRatingPair.getKey(),
                currentTeamPlayerRatingPair.getValue(),
                otherTeamPlayerRatingPair.getKey(),
                otherTeamPlayerRatingPair.getValue(),
                comparison);
            }
          }
        }
      }
    }

    val result = new HashMap[IPlayer, Rating]();

    for (currentTeam <- teamsList) {
      for (
        currentTeamPlayerPair <- currentTeam
          .entrySet()
      ) {
        val aa = deltas.get(
          currentTeamPlayerPair.getKey()).values();
        val currentPlayerAverageDuellingDelta = MathUtils.mean(aa);
        result.put(currentTeamPlayerPair.getKey(), new EloRating(
          currentTeamPlayerPair.getValue().getMean()
            + currentPlayerAverageDuellingDelta));
      }
    }

    return result;
  }

  private def updateDuels(
    gameInfo: GameInfo,
    duels: Map[IPlayer, Map[IPlayer, Double]],
    player1: IPlayer,
    player1Rating: Rating,
    player2: IPlayer,
    player2Rating: Rating,
    weakToStrongComparison: PairwiseComparison) {

    val t1 = Team.concat(new Team(player1, player1Rating), new Team(player2, player2Rating))

    val duelOutcomes = if (weakToStrongComparison == PairwiseComparison.WIN)
      twoPlayerEloCalculator.calculateNewRatings(gameInfo, t1, 1, 2)
    else if (weakToStrongComparison == PairwiseComparison.LOSE)
      twoPlayerEloCalculator.calculateNewRatings(gameInfo, t1, 2, 1)
    else twoPlayerEloCalculator.calculateNewRatings(gameInfo, t1, 1, 1)

    updateDuelInfo(duels, player1, player1Rating,
      duelOutcomes.get(player1), player2);
    updateDuelInfo(duels, player2, player2Rating,
      duelOutcomes.get(player2), player1);
  }

  override def calculateMatchQuality(gameInfo: GameInfo, teams: Collection[ITeam]): Double = {
    // HACK! Need a better algorithm, this is just to have something there
    // and it isn't good
    var minQuality = 1.0;

    val teamList = teams.toArray(new Array[ITeam](0))

    for (ixCurrentTeam <- 0 until teamList.length) {
      val currentTeamAverageRating = new EloRating(
        Rating.calcMeanMean(teamList(ixCurrentTeam).values()));
      val currentTeam = new Team(new Player[Integer](ixCurrentTeam),
        currentTeamAverageRating);

      for (ixOtherTeam <- ixCurrentTeam + 1 until teamList.length) {
        val otherTeamAverageRating = new EloRating(
          Rating.calcMeanMean(teamList(ixOtherTeam).values()));
        val otherTeam = new Team(new Player[Integer](ixOtherTeam),
          otherTeamAverageRating);

        minQuality = Math.min(
          minQuality, twoPlayerEloCalculator.calculateMatchQuality(gameInfo,
            Team.concat(currentTeam, otherTeam)));
      }
    }

    return minQuality;
  }

  def updateDuelInfo(
    duels: Map[IPlayer, Map[IPlayer, Double]],
    self: IPlayer,
    selfBeforeRating: Rating,
    selfAfterRating: Rating,
    opponent: IPlayer) {
    var selfToOpponentDuelDeltas = duels.get(self);

    if (selfToOpponentDuelDeltas == null) {
      selfToOpponentDuelDeltas = new HashMap[IPlayer, Double]();
      duels.put(self, selfToOpponentDuelDeltas);
    }

    selfToOpponentDuelDeltas.put(opponent, selfAfterRating.getMean()
      - selfBeforeRating.getMean());
  }
}