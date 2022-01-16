/**
 *
 */
package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.Step.Type;

/**
 * @author daniellink
 *
 */
public class Input {

	enum GameMode {
		Single, Game, Self // Self is an undocumented game mode that causes the agent to play against
							// itself
	}

	enum ReadState {
		Mode, Color, Time, Board
	}

	static GameMode mode;
	static ReadState currentState;
	static Double gameTime;

	static int currentBoardLine = 0;
	static Board inputBoard = new Board();

	public static void readInputFile() {
		// System.out.println("Working Directory for input file = " +
		// System.getProperty("user.dir"));
		currentState = ReadState.Mode;
		List<String> list = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get("input.txt"))) {
			list = stream.filter(line -> !(line.isBlank() || line.isEmpty())).collect(Collectors.toList());
		} catch (final IOException e) {
			System.err.println("Unable to read input.txt");
			System.exit(-1);
		}
		list.forEach(line -> processInputLine(line));
	}

	public static LinkedList<Step> readOutputFile() {
		LinkedList<Step> stepList = new LinkedList<>();
		// System.out.println("Working Directory for output file = " +
		// System.getProperty("user.dir"));
		List<String> list = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get("output.txt"))) {
			list = stream.filter(line -> !(line.isBlank() || line.isEmpty())).collect(Collectors.toList());
		} catch (final IOException e) {
			System.err.println("Unable to read output.txt");
			System.exit(-1);
		}
		list.forEach(line -> stepList.add(processOutputLine(line)));

		int index = 0;
		Step.Type moveType = null;
		;
		for (Step s : stepList) {
			if (index == 0) {
				moveType = s.getKind();
			}
			if (index > 0) {
				if (moveType == Type.SHOVE) {
					System.err.println("Non-jump moves can only have one step");
					System.exit(-1);
				}
				if (s.getKind() != moveType) {
					System.err.println("Cannot change step type after the first step");
				}
			}
		}
		return stepList;
	}

	private static Step processOutputLine(final String l) {
		String currentLine = l.toLowerCase().trim();
		Step s = new Step(currentLine);
		return s;
	}

	private static void processInputLine(final String l) {
		final String currentLine = l.trim();
		switch (currentState) {
		case Mode:
			switch (currentLine) {
			case "SINGLE" -> {
				mode = GameMode.Single;
				currentState = ReadState.Color;
				return;
			}
			case "GAME" -> {
				mode = GameMode.Game;
				currentState = ReadState.Color;
				return;
			}
			case "SELF" -> {
				mode = GameMode.Self;
				currentState = ReadState.Color;
				return;
			}
			default -> {
				System.err.println("Game mode " + currentLine + " unknown");
				System.exit(-1);
			}
			}
		case Color:
			switch (currentLine) {
			case "WHITE":
				inputBoard.setPlayerUp(Board.Color.WHITE);
				currentState = ReadState.Time;
				return;
			case "BLACK":
				inputBoard.setPlayerUp(Board.Color.BLACK);
				currentState = ReadState.Time;
				return;
			default:
				System.err.println("Color " + currentLine + " unknown");
				System.exit(-1);
			}
		case Time:
			gameTime = Double.parseDouble(currentLine);
			currentState = ReadState.Board;
			return;
		case Board:
			for (int i = 0; i < 8; i++) {
				inputBoard.putPiece(i, currentBoardLine, currentLine.charAt(i));
			}
			if (currentBoardLine++ > 7) {
				System.err.println("Attempt to read past 8 lines");
				System.exit(-1);
			}
			return;
		default:
			System.err.println("Read state undefined");
			System.exit(-1);
		}
	}

	public static String getGameModeString() {
		String gameMode = "NULL";
		switch (mode) {
		case Single -> gameMode = "SINGLE";
		case Game -> gameMode = "GAME";
		default -> throw new IllegalArgumentException("Unexpected value: " + mode);
		}
		return gameMode;

	}
}
