/**
 *
 */
package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import main.Board.Color;

/**
 * @author daniellink
 *
 */
public class Game {

	private static Board.Color nextUp;
	private static int maxDepth = 20;// Maximum depth when following jump moves

	public static int getMaxDepth() {
		return maxDepth;
	}

	private static int minDepth = 4; // Guaranteed depth for any move type

	public static int getMinDepth() {
		return minDepth;
	}

	public static Board.Color getNextUp() {
		return nextUp;
	}

	public static void setNextUp(final Board.Color nextUp) {
		Game.nextUp = nextUp;
	}

	public static void makeOneMove() {
		final Instant start = Instant.now();
		Board gameBoard = new Board(Input.inputBoard);
		nextUp = Input.inputBoard.getPlayerUp();
		if (Input.mode == Input.GameMode.Single) {
			minDepth = 1;
			maxDepth = 1;
		} else {
			if (Input.gameTime < 100) {
				minDepth--;
			}
//			if (Input.gameTime < 70) {
//				minDepth--;
//			}
//			if (Input.gameTime < 30) {
//				minDepth--;
//			}
			if (Input.gameTime < 10) {
				minDepth--;
			}
			if (Input.gameTime < 2) {
				minDepth--;
			}
		}
		System.out.println("Min depth = " + minDepth + ", max depth = " + maxDepth);
		final Move foundMove = getBestMove(gameBoard, nextUp, maxDepth);
		gameBoard = foundMove.getResultingBoard();
		System.out.println("Best move = \n" + foundMove);
		writeOutputFile(foundMove);
		System.out.println(gameBoard);
		final Instant end = Instant.now();
		final Duration timeElapsed = Duration.between(start, end);
		System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
	}

	public static void writeOutputFile(final Move m) {
		final Path path = Paths.get("output.txt");
		if (Files.exists(path)) {
			try {
				Files.delete(path);
			} catch (final IOException e) {
				System.err.println("Unable to delete existing output.txt");
			}
		}
		try {
			Files.writeString(Paths.get("output.txt"), m.toString());
		} catch (final IOException e) {
			System.err.println("Unable to write output.txt");
		}
	}

	public static void play() {
		Board gameBoard = new Board(Input.inputBoard);
		int plyCount = 0;
		nextUp = Input.inputBoard.getPlayerUp();
		System.out.println("Initial board:");
		gameBoard.computeHeuristic();
		System.out.println(gameBoard);
		// generate and evaluate all moves and generate their boards
		final Instant start = Instant.now();
		Duration longestMoveTime = Duration.ZERO;
		Duration shortestMoveTime = Duration.ofDays(999999);
		long averageMoveTime;
		while (gameBoard.getWinner() == Color.NONE) {
			final Instant beforeMove = Instant.now();
			Board.setHeuristicsComputedCount(0);
			Move.setMovesGeneratedCount(0);
			final Move foundMove = getBestMove(gameBoard, nextUp, maxDepth);
			final Instant afterMove = Instant.now();
			final Duration moveTimeElapsed = Duration.between(beforeMove, afterMove);
			if (moveTimeElapsed.toMillis() > longestMoveTime.toMillis()) {
				longestMoveTime = moveTimeElapsed;
			}
			if (moveTimeElapsed.toMillis() < shortestMoveTime.toMillis()) {
				shortestMoveTime = moveTimeElapsed;
			}
			System.out.println("Ply count = " + plyCount++);
			averageMoveTime = Duration.between(start, Instant.now()).toMillis() / plyCount;
			System.out.println("Calculated move in " + moveTimeElapsed.toMillis() + "ms");
			System.out.println("Timings (ms): min = " + shortestMoveTime.toMillis() + ", avg = " + averageMoveTime
					+ ", max = " + longestMoveTime.toMillis());
			System.out.println("Heuristics computed count = " + Board.getHeuristicsComputedCount());
			System.out.println("Moves generated count = " + Move.getMovesGeneratedCount());
			if (foundMove == null) {
				System.out.println("Player " + nextUp + " is unable to make a move and has lost");
				System.exit(0);
			}
			System.out.println("Best move (Value = " + foundMove.getValue() + ") = \n" + foundMove);
			gameBoard = foundMove.getResultingBoard();

			System.out.println(gameBoard);
			nextUp = Board.getOtherColor(nextUp);
		}
		final Instant end = Instant.now();
		System.out.println("Game over, winner: " + gameBoard.getWinner());
		final Duration timeElapsed = Duration.between(start, end);
		System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
		System.out.println("Plys per second = " + plyCount * 1000.0 / timeElapsed.toMillis());
	}

	public static Move getBestMove(final Board moveBoard, final Board.Color nextUp, final int depth) {
		moveBoard.setPlayerUp(nextUp);
		moveBoard.generateAllMoves(depth);
		final Move bestMove = moveBoard.findBestMove();
		return bestMove;
	}
}
