/**
 *
 */
package main;

import java.util.LinkedList;

/**
 * @author daniellink
 *
 */
public class Move {

	private final LinkedList<Step> steps;
	private final Board resultingBoard;

	public Board getResultingBoard() {
		return resultingBoard;
	}

	private final Board.Color player;
	private int value; // How good this move is based on minimax
	private final int depth;
	private static int movesGeneratedCount = 0;

	/**
	 * @return the movesGeneratedCount
	 */
	public static int getMovesGeneratedCount() {
		return movesGeneratedCount;
	}

	/**
	 * @param movesGeneratedCount the movesGeneratedCount to set
	 */
	public static void setMovesGeneratedCount(int movesGeneratedCount) {
		Move.movesGeneratedCount = movesGeneratedCount;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Calculate the value of this move, depending on the depth we're at
	 */
	private void calculateValue() {
		if (depth > Game.getMaxDepth() - Game.getMinDepth() || steps.peek().getKind() == Step.Type.JUMP) {
			resultingBoard.otherPlayerUp();
			resultingBoard.generateAllMoves(depth - 1);
			final Move bestMove = resultingBoard.findBestMove();
			if (bestMove == null) {
				if (resultingBoard.getPlayerUp() == Board.Color.BLACK) {
					value = Integer.MAX_VALUE;
				} else {
					value = Integer.MIN_VALUE;
				}
			} else {
				value = bestMove.getValue();
			}
			// Added to save memory
			resultingBoard.resetPossibleMoves();
			return;
		}
		value = resultingBoard.getHeuristic(); // Base case
	}

	public Move(final Board.Color player_, final Board before_, final LinkedList<Step> sequence, final int depth_) {
		player = player_;
		depth = depth_;
		resultingBoard = new Board(before_);
		steps = sequence;
		resultingBoard.executeStepList(sequence);
		resultingBoard.computeHeuristic();
		calculateValue();
		movesGeneratedCount++;
	}

	public boolean hasEqualSteps(LinkedList<Step> otherSteps) {
		if (steps.size() != otherSteps.size()) {
			return false;
		}
		for (int i = 0; i < steps.size(); i++) {
			if (!steps.get(i).equals(otherSteps.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String output = "";
		for (final Step s : steps) {
			output += s.getKind() == Step.Type.SHOVE ? "E " : "J ";
			output += s;
		}
		return output;
	}

	public Board.Color getPlayer() {
		return player;
	}

}
