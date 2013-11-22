package org.bcard.signal;

/**
 * Operators that can be used to combine our signals.
 * 
 * @author bcard
 *
 */
public enum CombineOperator implements Func2<Long, Long, Long> {
	/**
	 * An add function that adds two values.
	 */
	ADD {
		@Override
		public Long call(Long arg1, Long arg2) {
			return arg1 + arg2;
		}
	};

}
