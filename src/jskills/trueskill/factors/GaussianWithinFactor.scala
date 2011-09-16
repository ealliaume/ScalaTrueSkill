package jskills.trueskill.factors

import jskills.numerics.GaussianDistribution._
import jskills.trueskill.TruncatedGaussianCorrectionFunctions._
import jskills.factorgraphs.Message
import jskills.factorgraphs.Variable
import jskills.numerics.GaussianDistribution

/**
 * Factor representing a team difference that has not exceeded the draw margin.
 * [remarks]See the accompanying math paper for more details.[/remarks]
 */
class GaussianWithinFactor(epsilon: Double, variable: Variable[GaussianDistribution])
  extends GaussianFactor(format("%s [= %4.3f", variable, epsilon)) {
  CreateVariableToMessageBinding(variable)

  override def getLogNormalization(): Double = {
    val marginal = variables.get(0).value
    val message = messages.get(0).value
    val messageFromVariable = divide(marginal, message)
    val mean = messageFromVariable.mean
    val std = messageFromVariable.standardDeviation
    val z = cumulativeTo((epsilon - mean) / std) - cumulativeTo((-epsilon - mean) / std)

    return -logProductNormalization(messageFromVariable, message) + Math.log(z)
  }

  override protected def updateMessage(message: Message[GaussianDistribution], variable: Variable[GaussianDistribution]): Double = {
    val oldMarginal = new GaussianDistribution(variable.value)
    val oldMessage = new GaussianDistribution(message.value)
    val messageFromVariable = divide(oldMarginal, oldMessage)

    val c = messageFromVariable.precision
    var d = messageFromVariable.precisionMean

    val sqrtC = Math.sqrt(c)
    val dOnSqrtC = d / sqrtC

    val epsilonTimesSqrtC = epsilon * sqrtC
    d = messageFromVariable.precisionMean

    val denominator = 1.0 - WWithinMargin(dOnSqrtC, epsilonTimesSqrtC)
    val newPrecision = c / denominator
    val newPrecisionMean = (d + sqrtC * VWithinMargin(dOnSqrtC, epsilonTimesSqrtC)) / denominator

    val newMarginal = fromPrecisionMean(newPrecisionMean, newPrecision)
    val newMessage = divide(prod(oldMessage, newMarginal), oldMarginal)

    // Update the message and marginal
    message.value = newMessage
    variable.value = newMarginal

    // Return the difference in the new marginal
    return sub(newMarginal, oldMarginal)
  }
}
