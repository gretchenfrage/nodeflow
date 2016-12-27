package com.phoenixkahlo.ptest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Responsible for the invocations of a specific method of a specific mockery.
 */
public class MethodMocker {

	/**
	 * A function that returns null, suitable for void methods.
	 */
	public static final Function<Object[], Void> VOID = args -> null;
	
	public static enum Mode {
		/**
		 * A queue of responses, with predicates that will be asserted.
		 */
		RESPONSE_QUEUE,
		/**
		 * A function for all further responses, with a predicate that will be
		 * asserted.
		 */
		PERSISTENT_RESPONSE,
		/**
		 * A set of predicates-response pairs. The first pair which's predicate
		 * tests true will have it's function used for the response.
		 */
		PREDICATE_RESPONSE_SET
	}

	private static class PredicateFunctionPair {

		Predicate<Object[]> predicate;
		Function<Object[], ? extends Object> function;

		PredicateFunctionPair(Predicate<Object[]> predicate, Function<Object[], ? extends Object> function) {
			this.predicate = predicate;
			this.function = function;
		}

	}

	private Mode mode;
	/**
	 * If in RESPONSE_QUEUE mode, responses is a queue that adds at position 0.
	 * If in PERSISTENT_RESPONSE mode, the first item is the response. If in
	 * PREDICATE_RESPONSE_SET, responses is the set of predicates and responses.
	 */
	private List<PredicateFunctionPair> responses;
	private String name;
	
	public MethodMocker(String name) {
		this.name = name;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		responses = new ArrayList<>();
	}

	public void queueResponse(Predicate<Object[]> predicate, Function<Object[], ? extends Object> function) {
		if (mode != Mode.RESPONSE_QUEUE)
			setMode(Mode.RESPONSE_QUEUE);
		responses.add(0, new PredicateFunctionPair(predicate, function));
	}

	public void queueResponse(Function<Object[], ? extends Object> function) {
		queueResponse(args -> true, function);
	}

	public void queueResponse(Object response) {
		queueResponse(args -> true, args -> response);
	}

	public void queueResponse(Predicate<Object[]> predicate, Object response) {
		queueResponse(predicate, args -> response);
	}
	
	public void queueAssert(Predicate<Object[]> predicate) {
		queueResponse(predicate, VOID);
	}
	
	public void expectResponse() {
		queueResponse(VOID);
	}

	public void setResponse(Predicate<Object[]> predicate, Function<Object[], ? extends Object> function) {
		if (mode != Mode.PERSISTENT_RESPONSE)
			setMode(Mode.PERSISTENT_RESPONSE);
		PredicateFunctionPair pair = new PredicateFunctionPair(predicate, function);
		if (responses.isEmpty())
			responses.add(pair);
		else
			responses.set(0, pair);
	}

	public void setResponse(Function<Object[], ? extends Object> function) {
		setResponse(args -> true, function);
	}

	public void setResponse(Object response) {
		setResponse(args -> true, args -> response);
	}

	public void setResponse(Predicate<Object[]> predicate, Object response) {
		setResponse(predicate, args -> response);
	}
	
	public void setAssert(Predicate<Object[]> predicate) {
		setResponse(predicate, VOID);
	}

	public void addResponse(Predicate<Object[]> predicate, Function<Object[], ? extends Object> response) {
		if (mode != Mode.PREDICATE_RESPONSE_SET)
			setMode(Mode.PREDICATE_RESPONSE_SET);
		responses.add(new PredicateFunctionPair(predicate, response));
	}

	public void addResponse(Predicate<Object[]> predicate, Object response) {
		addResponse(predicate, args -> response);
	}
	
	public void assertQueueEmpty() {
		if (mode == Mode.RESPONSE_QUEUE)
			assert responses.isEmpty();
		else
			throw new IllegalStateException("assertQueueEmpty invoked while not in queue mode");
	}

	Object handle(Object[] args) {
		if (mode == null)
			throw new IllegalStateException("Mode not determined at " + name);
		switch (mode) {
		case RESPONSE_QUEUE:
			if (responses.isEmpty())
				throw new Error("No responses queued");
			PredicateFunctionPair pair = responses.remove(responses.size() - 1);
			assert pair.predicate.test(args);
			return pair.function.apply(args);
		case PERSISTENT_RESPONSE:
			assert responses.get(0).predicate.test(args);
			return responses.get(0).function.apply(args);
		case PREDICATE_RESPONSE_SET:
			Optional<PredicateFunctionPair> match = responses.stream().filter(potential -> potential.predicate.test(args))
					.findAny();
			if (!match.isPresent())
				throw new Error("No response found");
			assert match.get().predicate.test(args);
			return match.get().function.apply(args);
		default:
			throw new IllegalStateException();
		}
	}

}
