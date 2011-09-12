package jskills;
import scala.reflect.BeanProperty
/**
 * Represents a player who has a {@link Rating}.
 */

class Player[T](
  /**
   * The identifier for the player, such as a name.
   */
  val id: T,
  /**
   * Indicates the percent of the time the player should be weighted where 0.0
   * indicates the player didn't play and 1.0 indicates the player played 100%
   * of the time.
   */
  @BeanProperty val partialPlayPercentage: Double = Player.DefaultPartialPlayPercentage,
  /**
   * Indicated how much of a skill update a player should receive where 0.0
   * represents no update and 1.0 represents 100% of the update.
   */
  @BeanProperty val partialUpdatePercentage: Double = Player.DefaultPartialUpdatePercentage)
  extends IPlayer with ISupportPartialPlay with ISupportPartialUpdate {

  //for java calls
  def this(id: T) { this(id, Player.DefaultPartialPlayPercentage, Player.DefaultPartialUpdatePercentage) }

  //for java calls
  def this(id: T, partialPlayPercentage: Double) { this(id, partialPlayPercentage, Player.DefaultPartialUpdatePercentage) }

  override def hashCode(): Int = {
    val prime = 31;
    prime + (if (id == null) 0 else id.hashCode());
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true;
    if (obj == null) return false;
    if (obj.isInstanceOf[Player[T]]) {
      val other: Player[T] = obj.asInstanceOf[Player[T]]
      if (id == null) {
        if (other.id != null) return false;
      } else if (!id.equals(other.id)) return false;
      return true;
    }
    return false;
  }

  override def toString() = if (id != null) id.toString() else super.toString()

}
object Player {
  /** = 100% play time **/
  val DefaultPartialPlayPercentage = 1.0;
  /** = receive 100% update **/
  val DefaultPartialUpdatePercentage = 1.0;
  /** The identifier for the player, such as a name. **/
}