/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.95e
 */
package org.scalaml.trading

import org.scalaml.ga.{Operator, Gene, Discretization}
import org.scalaml.trading.operator._
import org.scalaml.core.types.ScalaMl._
import scala.annotation.implicitNotFound
import scala.collection.mutable.ListBuffer
import org.scalaml.core.XTSeries
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import org.scalaml.util.Display
import scala.collection.mutable.TreeSet



		/**
		 * <p>Define a trading signal as used in technical analysis of financial markets. A partial
		 * list of trading signals, include volume, Flow index, momentum or oscillators. This class
		 * inherit the <b>Gene</b> class so trading signals can be threaded into trading strategy implemented
		 * as chromosomes of a genetic algorithm.<br>
		 * A trading signal is emitted once a value (or data point) in a time series reaches a threshold (upward or downward movement).<br>
		 * A signal is triggers when x(n) > target value or x(n) < target value<br>
		 * The class assume that a digitization function that discrete a continuous value is defined
		 * implicitly.<br>
		 * <b>id</b> Label or identifier for the trading signal.<br>
		 * <b>target</b> Target value (or threshold) used to trigger the signal.<br>
		 * <b>op</b> Operator that is used to defined the condition such as greater than, equals.... <br>
		 * <b>xt</b> Times series of single variable the signal acts upon.<br>
		 * <b>weights</b> Weights applied to each value of the time series (optional).<br>
		 * <b>discr</b> Discretization function that convert analog or continuous signal to a discrete time series.</p>
		 * @constructor Create a trading signal used for analyzing changes in variables derived from the price and trading volume of a security. 
		 * @throws IllegalArgumentException if the class parameters are not properily defined.
		 * @throws ImplicitNotFoundException if the disretization function has not been defined.
		 * @see org.scalaml.ga.Gene
		 * 
		 * @author Patrick Nicolas
		 * @note Scala for Machine Learning
		 * @since March 4, 2014 Appendix Finances 101 / Technical analysis
		 */
@implicitNotFound("Signal does not have a discretization function implicitly defined")
class Signal(_id: String, _target: Double, _op: Operator, xt: DblVector, weights: DblVector)(implicit discr: Discretization) 
					extends Gene(_id, _target, _op) {
	import Signal._
	check(_id, _target, _op, xt, weights)
   
		/**
		 * Computation of the score of this trading signal by comparing a value with the threshold, value
		 * @param x value to compare with the target value
		 * @param factor amplification factor for the generation of the score
		 * @return computed score for this trading signal
		 */
	override def score: Double = sumScore(operatorFuncMap.get(op).get)
    
		/**
		 * Compare this trading signal with another one
		 * @param that  other trading signal
		 * @return true if the two trading signals share the same operator and threshold value
		 */
	final def == (that: Signal): Boolean = op == that.op && Math.abs(target - that.target) < EPS
   
		/**
		 * <p>Description of the trading signal using the encoded value of the target</p>
		 * @return tuple (id, operator, encoded target value)
		 */
	override def toString: String = s"$id ${op.toString} ${String.valueOf(target)}"
   
	private def sumScore(f: (Double, Double) => Double): Double = 
		xt.zip(weights).foldLeft(0.0)((s, x) => s + x._2*f(x._1, target))
   
}



	/**
	 * Companion object to the trading signal class, used to defined constructors.
	 */
object Signal {
	final val EPS = 1e-3
	final val CSV_DELIM = ",";

	def apply(id: String, target: Double, op: Operator, obs: DblVector, weights: DblVector)(implicit discr: Discretization): Signal = 
		new Signal(id, target, op, obs, weights)
	
	def apply(id: String, target: Double, op: Operator)(implicit discr: Discretization): Signal = 
		new Signal(id, target, op, Array.empty, Array.empty)

	val orderedSignals = Ordering.by((signal: Signal) => signal.id)
   
	val operatorFuncMap = Map[Operator, (Double, Double) =>Double](
		LESS_THAN -> ((x: Double, target: Double) => target - x),
		GREATER_THAN -> ((x: Double, target: Double) => x -target),
		EQUAL -> ((x: Double, target: Double) => Math.abs(x -target)),
		NONE -> ((x: Double, target: Double) => -1.0)
	)
   
	@inline
	final def numOperators = operatorFuncMap.size
   
	final private val MAX_TIME_SERIES_SIZE = 10000000
	
	protected def check(_id: String, _target: Double, _op: Operator, xt: DblVector, weights: DblVector): Unit = {
		require( _op != null, "Signal.check Cannot create a signal with undefined operator")
		require(xt != null, "Signal.check Cannot create a signal with undefined time series input")
		require(xt.size > 0 && xt.size < MAX_TIME_SERIES_SIZE, s"Signalcheck Size of the time series input, ${xt.size} if out of range")
		require(weights != null, "Signal.check Cannot create a signal with undefined weights")
		require(weights.size > 0 && weights.size < MAX_TIME_SERIES_SIZE, s"Signalcheck Number of weights ${weights.size} if out of range")
		require(xt.size == weights.size, s"Signal The number of weights ${xt.size} is different from the size of data ${xt.size}")
	}
}

// ------------------------ EOF --------------------------------------------------------