package net.finmath.montecarlo.automaticdifferentiation.backward;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.montecarlo.AbstractRandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.automaticdifferentiation.RandomVariableDifferentiable;
import net.finmath.stochastic.RandomVariable;

/**
 * Basic test for RandomVariableDifferentiableAAD.
 *
 * @author Christian Fries
 */
public class RandomVariableDifferentiableAADTest {

	@Test
	public void testSecondOrderDerivative() {

		/*
		 * Note: This is still experimental, but it shows that second order derivatives are possible by
		 * differentiation of the first order derivative.
		 * Handling and assignment of IDs is subject to change.
		 */
		AbstractRandomVariableFactory randomVariableFactoryParameters = new RandomVariableFactory();

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("isGradientRetainsLeafNodesOnly", false);

		AbstractRandomVariableFactory randomVariableFactoryVariable = new RandomVariableDifferentiableAADFactory(
				new RandomVariableDifferentiableAADFactory(
						new RandomVariableFactory(), properties), properties);

		RandomVariable a = randomVariableFactoryParameters.createRandomVariable(5.0);
		RandomVariable b = randomVariableFactoryParameters.createRandomVariable(1.0);
		RandomVariableDifferentiable x = (RandomVariableDifferentiable) randomVariableFactoryVariable.createRandomVariable(5.0);
		RandomVariableDifferentiable y = (RandomVariableDifferentiable) randomVariableFactoryVariable.createRandomVariable(2.0);

		// Simple function: (a x)^2 + (b y)^3
		RandomVariableDifferentiable result = (RandomVariableDifferentiable)x.mult(a).pow(2).add(y.mult(b).pow(3));

		// Get first derivative
		Map<Long, RandomVariable> dresult = result.getGradient();
		RandomVariableDifferentiable dx = (RandomVariableDifferentiable)dresult.get(x.getID());

		// Get second order derivative
		Map<Long, RandomVariable> ddx = dx.getGradient();
		RandomVariable ddxx = ddx.get(x.getID()-1);				// The -1 here is subject to change. Currently an issue that we need to access the inner ID.

		double derivativeAAD = ddxx.getAverage();
		double derivativeExpected = 2 * a.pow(2).mult(x.pow(0)).doubleValue();

		System.out.println("AAD value     : " + derivativeAAD);
		System.out.println("expeted value : " + derivativeExpected);
		System.out.println();

		Assert.assertEquals(derivativeExpected, derivativeAAD, 1E-15);
	}
}
