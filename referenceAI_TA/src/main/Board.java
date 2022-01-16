/**
 *
 */
package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author daniellink
 *
 */
public class Board {

	public enum Piece {
		EMPTY, BMAN, BKING, WMAN, WKING
	}

	public enum Color {
		WHITE, BLACK, NONE
	}

	private Piece[][] position;
	private int heuristic; // Static evaluation of the board based on where the pieces are
	private boolean heuristicComputed;
	private ArrayList<Move> possibleMoves;
	private Move bestMove;
	private int plyCount;
	private Color playerUp;
	private int direction; // direction that simple pieces go in
	private int depth;
	private boolean canJump = false; // Whether at least one piece can jump
	private Color winner = Color.NONE;
	private static boolean debug = false;
	private static int heuristicsComputedCount = 0;

	/**
	 * @return the heuristicsComputedCount
	 */
	public static int getHeuristicsComputedCount() {
		return heuristicsComputedCount;
	}

	/**
	 * @param heuristicsComputedCount the heuristicsComputedCount to set
	 */
	public static void setHeuristicsComputedCount(int heuristicsComputedCount) {
		Board.heuristicsComputedCount = heuristicsComputedCount;
	}

	public boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		Board.debug = debug;
	}

	public Color getWinner() {
		return winner;
	}

	static Random rand = new Random();

	public Color getPlayerUp() {
		return playerUp;
	}

	public void setPlayerUp(final Color playerUp) {
		if (playerUp == Color.NONE) {
			System.err.println("Player color can't be 'NONE'!");
			System.exit(-1);
		}
		this.playerUp = playerUp;
		direction = -1;
		if (playerUp == Color.BLACK) {
			direction = 1;
		}
	}

	public void resetPossibleMoves() {
		possibleMoves.clear();
	}

	public int getPlyCount() {
		return plyCount;
	}

	public void setPlyCount(final int plyCount) {
		this.plyCount = plyCount;
	}

	public void incrementPlyCount() {
		plyCount++;
	}

	public Move getBestMove() {
		return bestMove;
	}

	public ArrayList<Move> getPossibleMoves() {
		return possibleMoves;
	}

	public int getHeuristic() {
		if (!heuristicComputed) {
			computeHeuristic();
		}
		return heuristic;
	}

	public Board() {
		position = new Piece[8][8];
		heuristic = 0;
		heuristicComputed = true;
	}

	public Board(final Board b) {
		position = new Piece[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				position[i][j] = b.position[i][j];
			}
		}
		playerUp = b.playerUp;
		direction = b.direction;
	}

	public void initialize() {
		position = new Piece[8][8];
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				position[x][y] = Piece.EMPTY;
			}
		}
		for (int y = 0; y < 3; y = y + 2) {
			for (int x = 0; x < 8; x = x + 2) {
				position[x][y] = Piece.WMAN;
				position[7 - x][7 - y] = Piece.BMAN;
			}
		}
		for (int x = 1; x < 8; x = x + 2) {
			position[x][1] = Piece.WMAN;
			position[7 - x][6] = Piece.BMAN;
		}
		playerUp = Color.BLACK;
	}

	@Override
	public String toString() {
		String output = "";
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				String piece = "X";
				final Piece p = position[x][y];
				switch (p) {
				case EMPTY:
					piece = ".";
					break;
				case BMAN:
					piece = "b";
					break;
				case WMAN:
					piece = "w";
					break;
				case BKING:
					piece = "B";
					break;
				case WKING:
					piece = "W";
					break;
				default:
					System.err.println("Invalid piece at position " + x + "," + y + ": " + p);
					System.exit(-1);
				}
				output = output.concat(piece);
			}
			// if (y < 7)
			if (debug) {
				output = output.concat("|");
				output = output.concat(Integer.toString(8 - y));
			}
			output = output.concat("\n");
		}
		if (debug) {
			output = output.concat("--------\nABCDEFGH\n");
			if (heuristicComputed) {
				output = output.concat("\nHeuristic = " + heuristic);
			}
		}
		return output;
	}

	public void putPiece(final int x, final int y, final char c) {
		switch (c) {
		case 'b' -> position[x][y] = Piece.BMAN;
		case 'w' -> position[x][y] = Piece.WMAN;
		case 'B' -> position[x][y] = Piece.BKING;
		case 'W' -> position[x][y] = Piece.WKING;
		case '.' -> position[x][y] = Piece.EMPTY;
		default -> {
			System.err.println("Trying to put invalid piece " + c + " at " + x + "," + y);
			System.exit(-1);
		}
		}
	}

	public void computeHeuristic() {// White maximizes, black minimizes
		if (!heuristicComputed) {
			boolean blackHasNoPieces = true;
			boolean whiteHasNoPieces = true;
			heuristic = 0;
			int offset = 1;
			int x;
			for (int y = 0; y < 8; y++) { // rows
				final int yCenterBonus = (7 - y) * y / 2;
				for (int i = 0; i < 4; i++) { // columns
					x = 2 * i + offset;
					final int xCenterBonus = (3 - i) * i;
					final Piece p = position[x][y];
					if (p != Piece.EMPTY) {
						switch (p) {
						case BMAN -> {
							blackHasNoPieces = false;
							heuristic -= 10 + y + xCenterBonus;
						}
						case WMAN -> {
							whiteHasNoPieces = false;
							heuristic += 17 - y + xCenterBonus;
						}
						case BKING -> {
							blackHasNoPieces = false;
							heuristic -= 50 + xCenterBonus + yCenterBonus;
						}
						case WKING -> {
							whiteHasNoPieces = false;
							heuristic += 50 + xCenterBonus + yCenterBonus;
						}
						case EMPTY -> {
							System.err.println("This should never be reached, added to get rid of warning");
							System.exit(-1);
						}
						}
					}
				}
				offset = 1 - offset;
			}
			if (blackHasNoPieces) {
				heuristic = Integer.MAX_VALUE;
				winner = Color.WHITE;
				heuristicComputed = true;
				return;
			}
			if (whiteHasNoPieces) {
				heuristic = Integer.MIN_VALUE;
				winner = Color.BLACK;
				heuristicComputed = true;
				return;
			}
			if (possibleMoves != null) {
				// System.out.println("Hello");
				if (possibleMoves.isEmpty()) {
					if (playerUp == Color.BLACK) {
						heuristic = Integer.MAX_VALUE;
						winner = Color.WHITE;
					}
					if (playerUp == Color.WHITE) {
						heuristic = Integer.MIN_VALUE;
						winner = Color.BLACK;
					}
				}
			}
			heuristicComputed = true;
			heuristicsComputedCount++;
		}
		return;
	}

	public static Color getOtherColor(final Color c) {
		final Color result = switch (c) {
		case WHITE -> Color.BLACK;
		case BLACK -> Color.WHITE;
		default -> null;
		};
		return result;
	}

	public void generateAllMoves(final int depth_) {
		depth = depth_;
		possibleMoves = new ArrayList<>();
		int offset = 1;
		for (int j = 0; j < 8; j++) { // rows
			for (int i = offset; i < 8; i = i + 2) { // columns
				if (getColor(i, j) == playerUp) {
					final ArrayList<Move> generated = generateMoves(i, j);
					if (generated != null) {
						possibleMoves.addAll(generated);
					}
				}
			}
			offset = 1 - offset;
		}
	}

	private Color getColor(final int x, final int y) {
		final Piece p = position[x][y];
		final Color result = switch (p) {
		case EMPTY -> Color.NONE;
		case BMAN -> Color.BLACK;
		case WMAN -> Color.WHITE;
		case BKING -> Color.BLACK;
		case WKING -> Color.WHITE;
		default -> null;
		};
		if (result == null) {
			System.err.println("Impossible Piece at " + x + "," + y);
			System.exit(-1);
		}
		return result;
	}

	private ArrayList<Move> generateMoves(final int xFrom, final int yFrom) {
		ArrayList<Move> foundMoves = new ArrayList<>(); // The list of moves we found from [x,y]
		final Piece currentPiece = position[xFrom][yFrom];
		int yMin = direction, yMax = direction;
		if (currentPiece == Piece.BKING || currentPiece == Piece.WKING) {
			yMin = -1;
			yMax = 1;
		}
		for (int x = -1; x < 2; x = x + 2) {
			for (int y = yMin; y < yMax + 1; y = y + 2) {
				try { // Single steps forward
					if (!canJump) {
						final Move singleMove = checkSingleMove(xFrom, yFrom, xFrom + x, yFrom + y);
						if (singleMove != null) {
							foundMoves.add(singleMove);
						}

					}
					final Step jump = checkJumpStep(currentPiece, xFrom, yFrom, xFrom + 2 * x, yFrom + 2 * y);
					if (jump != null) {
						if (!canJump) {
							foundMoves = new ArrayList<>();
							possibleMoves = new ArrayList<>();
							canJump = true;
						}
						final Move jumpMove = new Move(playerUp, this, jump.toLongestStepSequence(), depth);
						foundMoves.add(jumpMove);
						while (jump.hasChildren()) {
							final Move childMove = new Move(playerUp, this, jump.toLongestStepSequence(), depth);
							foundMoves.add(childMove);
						}
					}
				} catch (final java.lang.ArrayIndexOutOfBoundsException e) { // If a move ends up outside of the board,
					continue;
				}
			}
		}
		return foundMoves;
	}

	private Move checkSingleMove(final int fromX, final int fromY, final int toX, final int toY) {
		if (position[toX][toY] == Piece.EMPTY) {
			final Step s = new Step(Step.Type.SHOVE, fromX, fromY, toX, toY);
			final LinkedList<Step> steps = new LinkedList<>();
			steps.add(s);
			final Move m = new Move(playerUp, this, steps, depth);
			return m;
		}
		return null;
	}

	private Step checkJumpStep(final Piece p, final int fromX, final int fromY, final int toX, final int toY) {
		final int middleX = (fromX + toX) / 2;
		final int middleY = (fromY + toY) / 2;
		final Color otherPlayer = getOtherColor(playerUp);
		if (!(position[toX][toY] == Piece.EMPTY && getColor(middleX, middleY) == otherPlayer)) {
			return null; // Must jump over piece of other player and to an empty position
		}
		final Step s = new Step(Step.Type.JUMP, fromX, fromY, toX, toY);
		int yMin = 0, yMax = 0;
		switch (p) {
		case BMAN, WMAN -> {
			yMin = direction;
			yMax = direction;
		}
		case BKING, WKING -> {
			yMin = -1;
			yMax = 1;
		}
		default -> {
			System.err.println("Impossible piece encountered while generating jump moves");
			System.exit(-1);
		}
		}

		// Check if we can go deeper
		for (int x = -1; x < 2; x = x + 2) { // -1 and 1 (left and right)
			for (int y = yMin; y < yMax + 1; y = y + 2) {
				try {
					if (getColor(toX + x, toY + y) != otherPlayer) {
						continue; // Must jump over other player's piece
					}
					final Board resultingBoard = new Board(this);
					resultingBoard.executeSingleStep(s);
					final Step jump = resultingBoard.checkJumpStep(p, toX, toY, toX + 2 * x, toY + 2 * y);
					if (jump != null) {
						s.addChild(jump);
					}
				} catch (final java.lang.ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}
		}
		return s;
	}

	public Move findBestMove() {
		Move bestMove = null;
		try {
			bestMove = possibleMoves.get(0);
		} catch (final java.lang.IndexOutOfBoundsException e) {
			return null; // No possible moves
		}
		for (final Move m : possibleMoves) {
			final int currentValue = m.getValue();
			final int bestMoveValue = bestMove.getValue();
			if (playerUp == Color.BLACK) {
				if (currentValue < bestMoveValue) {
					bestMove = m;
				}
			} else {
				if (currentValue > bestMoveValue) {
					bestMove = m;
				}
			}
			if (currentValue == bestMoveValue) { // If the other move has the same value, randomize
				final int rnd = rand.nextInt(2);
				if (rnd == 1) {
					bestMove = m;
				}
			}
		}
		// Delete the lower moves to reclaim memory
		possibleMoves = new ArrayList<>();
		return bestMove;
	}

	public boolean executeStepList(final LinkedList<Step> steps) {
		boolean promotion = false;
		heuristicComputed = false;
		for (final Step s : steps) {
			promotion = executeSingleStep(s);
		}
		computeHeuristic();
		return promotion;
	}

	public boolean executeSingleStep(final Step s) {
		boolean promotion = false;
		final Piece pFrom = position[s.getxFrom()][s.getyFrom()];
		if (pFrom == Piece.EMPTY) {
			System.err.println("Attempt to execute invalid step  " + s);
			System.exit(-1);
		}
		final int xTo = s.getxTo();
		final int yTo = s.getyTo();
		position[xTo][yTo] = pFrom;
		switch (yTo) { // Promote man to king as applicable
		case 7:
			if (pFrom == Piece.BMAN) {
				position[xTo][yTo] = Piece.BKING;
				promotion = true;
			}
			break;
		case 0:
			if (pFrom == Piece.WMAN) {
				position[xTo][yTo] = Piece.WKING;
				promotion = true;
			}
			break;
		default:
		}
		final int xFrom = s.getxFrom();
		final int yFrom = s.getyFrom();
		position[xFrom][yFrom] = Board.Piece.EMPTY; // Empty out the original position
		if (s.getKind() == Step.Type.JUMP) { // If the piece jumps over a piece of the other player,
												// capture it
			final int xMiddle = (xFrom + xTo) / 2;
			final int yMiddle = (yFrom + yTo) / 2;
			position[xMiddle][yMiddle] = Piece.EMPTY;
		}
		return promotion;
	}

	public static String positionToNotation(final int x, final int y) {
		return (char) ('a' + x) + "" + (8 - y);
	}

	public static String getColorString(Color c) {
		String colorString = "NULL";
		switch (c) {
		case WHITE -> colorString = "WHITE";
		case BLACK -> colorString = "BLACK";
		case NONE -> colorString = "NONE";
		}
		return colorString;
	}

	public Color otherPlayerUp() {
		playerUp = Board.getOtherColor(playerUp);
		direction = -direction;
		canJump = false;
		possibleMoves = null;
		bestMove = null;
		return playerUp;
	}
}
