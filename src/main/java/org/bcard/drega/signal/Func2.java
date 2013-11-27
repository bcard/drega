package org.bcard.drega.signal;

/**
 * Function with two arguments.
 * 
 * @author bcard
 * 
 * @param <R>
 *            the return value
 * @param <T0>
 *            the first argument
 * @param <T1>
 *            the second argument
 */
public interface Func2<R, T0, T1> {

	/**
	 * Call the function
	 * 
	 * @param arg1
	 *            the first argument
	 * @param arg2
	 *            the second argument
	 * @return a value
	 */
	public R call(T0 arg1, T1 arg2);
}
