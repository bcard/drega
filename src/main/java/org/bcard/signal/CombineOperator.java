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
	ADD(new Func2<Long, Long, Long>() {

		@Override
		public Long call(Long arg1, Long arg2) {
			return arg1 + arg2;
		}
		
	});
	
	private Func2<Long, Long, Long> function;
	
	private CombineOperator(Func2<Long, Long, Long> function) {
		this.function = function;
	}

	@Override
	public Long call(Long arg1, Long arg2) {
		return function.call(arg1, arg2);
	}

}
