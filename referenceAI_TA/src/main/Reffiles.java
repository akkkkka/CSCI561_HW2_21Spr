package main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reffiles {

	// Contents of the data directory //
	// ---
	// Boards since the last material change
	// Contents get erased each time a material change occurs

	static String dataDirName = "referee_board_data";
	static String plyCountFileName = "current_ply.txt";
	static String lastMaterialChangeFileName = "lastmatchange_ply.txt";
	static ArrayList<String> seenPositions = new ArrayList<>();

	public static void createDataDir() {
		if (!Files.isDirectory(Paths.get(dataDirName))) {
			try {
				Files.createDirectory(Paths.get(dataDirName));
			} catch (final IOException e1) {
				System.err.println("Unable to create directory " + dataDirName);
			}
		}
	}

	public static boolean findThreepeatedPositions() {
		int equalPositionsSeen;
		readAllPositions();
		final int originalSize = seenPositions.size();
		for (int i = 0; i < originalSize; i++) {
			final String s = seenPositions.remove(0);
			equalPositionsSeen = 0;
			for (final String t : seenPositions) {
				if (s.equals(t)) {
					if (++equalPositionsSeen >= 2) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static void readAllPositions() {
		List<Path> allPaths = null;
		;
		try (Stream<Path> walk = Files.walk(Paths.get(dataDirName))) {
			allPaths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (final IOException e) {
			System.err.println("Couldn't access board files from directory '" + dataDirName + "'");
			System.exit(-1);
		}
		for (final Path p : allPaths) {
			seenPositions.add(readStringFromFile(dataDirName + File.separator + p.getFileName()));
		}
	}

	public static void writeDefaultNext() {
		writeStringToFile("FAIL", "next.txt");
	}

	public static void writeToNext(String s) {
		writeStringToFile(s, "next.txt");
	}

	public static void writeNextWithContent() {
		final String nextPlayer = Input.inputBoard.getPlayerUp().toString();
		final String gameMode = Input.getGameModeString();
		final String timeString = Input.gameTime.toString();
		final String boardString = Input.inputBoard.toString();
		writeStringToFile(gameMode + "\n" + nextPlayer + "\n" + timeString + "\n" + boardString, "next.txt");
	}

	public static void writeStringToFile(String s, String fn) {
		final Path p = Paths.get(fn);
		try {
			Files.deleteIfExists(p);
			Files.writeString(p, s, StandardCharsets.ISO_8859_1);
		} catch (final IOException e) {
			System.err.println("Couldn't write to file '" + fn + "'");
			System.exit(-1);
		}
	}

	public static String readStringFromFile(String fn) {
		final Path p = Paths.get(fn);
		if (!Files.exists(p)) {
			System.err.println("Couldn't read file '" + fn + "'");
		}
		String content = null;
		try {
			content = Files.readString(p);
		} catch (final IOException e) {
			System.err.println("Couldn't read content from file '" + fn + "'");
		}
		return content;
	}

	public static boolean increasePlyCountFile(boolean matChange) {
		final String plyCountString = readStringFromFile(plyCountFileName);
		final String matChangeString = readStringFromFile(lastMaterialChangeFileName);
		if (plyCountString == null) {
			writeStringToFile("1", plyCountFileName);
			writeStringToFile("1", lastMaterialChangeFileName);
			return false;
		}
		int plyCount = Integer.parseInt(plyCountString);
		final int matChangePly = Integer.parseInt(matChangeString);
		if (plyCount - matChangePly >= 50) {
			System.out.println("Ain't nothing going on but the rent");
			return true;
		}
		writeStringToFile(String.valueOf(++plyCount), plyCountFileName);
		if (matChange) {
			writeStringToFile(String.valueOf(plyCount), lastMaterialChangeFileName);
		}
		return false;
	}

	public static void cleanDataDirectory() {
		try (Stream<Path> walk = Files.walk(Paths.get(dataDirName))) {
			walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		} catch (final IOException e) {
			System.err.println("Couldn't clean out data directory");
			System.exit(-1);
		}
		createDataDir();
	}

}
