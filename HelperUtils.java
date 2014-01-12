
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HelperUtils {

	private static HelperLogger logger = HelperLogger.getLogger(HelperUtils.class);

	public static List<String> parseAnswers(String answers) {
		List<String> parsedList = new ArrayList<String>();
		Scanner scanner = new Scanner(answers);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.isEmpty()) {
				continue;
			}
			parsedList.add(line.trim());
		}
		scanner.close();
		return parsedList;
	}

	public static Map<String, String> parseRecipients(String recipients) {
		HashMap<String, String> parsedMap = new HashMap<>();
		Scanner scanner = new Scanner(recipients);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.isEmpty()) {
				continue;
			}
			int indexOfColon = line.indexOf(":");
			if (indexOfColon == -1) {
				parsedMap.put(line, null);
			} else {
				parsedMap.put(line.substring(0, indexOfColon).trim(), line.substring(indexOfColon + 1).trim());
			}
		}
		scanner.close();
		return parsedMap;
	}

	public static String readRequestHeaders(BufferedReader is) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String nextLine = is.readLine();
		while (nextLine != null) {
			if (nextLine.isEmpty()) {
				break;
			}
			stringBuilder.append(nextLine + "\r\n");
			nextLine = is.readLine();
		}
		return stringBuilder.toString();
	}

	public static String readRequestBody(BufferedReader is, int contentLength) throws IOException {
		CharBuffer buffer = CharBuffer.allocate(contentLength);
		is.read(buffer);
		return buffer.rewind().toString();
	}

	public static String readRequestBody(BufferedReader is, HTTPRequest httpRequest) throws IOException,
			WebServerBadRequestException {
		CharBuffer buffer;
		try {
			Integer contentLength = httpRequest.getContentLength();
			if (contentLength == null) {
				throw new WebServerBadRequestException(
						"Error. Tried to get request body but content-length header doesn't exist.");
			}
			buffer = CharBuffer.allocate(httpRequest.getContentLength());
		} catch (WebServerBadRequestException e) {
			throw new WebServerBadRequestException(
					"Error. Tried to get request body but content-length header doesn't exist.");
		}
		is.read(buffer);
		return buffer.rewind().toString();
	}

	public static void parseParameters(Map<String, String> parametersMap, String params) {
		if (params.isEmpty()) {
			return;
		}
		Scanner scanner = new Scanner(params);
		scanner.useDelimiter("&");
		try {
			while (scanner.hasNext()) {
				try {
					String parameter = scanner.next();
					HelperUtils.parseParameter(parametersMap, parameter);
				} catch (WebServerRuntimeException e) {
					logger.warn("Could not parse parameter: '" + "'. Skipping this parameter.");
				}
			}
		} finally {
			scanner.close();
		}
	}

	public static byte[] readFile(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] bFile = new byte[(int) file.length()];
			// read until the end of the stream.
			while (fis.available() != 0) {
				fis.read(bFile, 0, bFile.length);
			}
			return bFile;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	private static void parseParameter(Map<String, String> parametersMap, String parameter)
			throws WebServerRuntimeException {
		Scanner scanner = new Scanner(parameter);
		scanner.useDelimiter("=");
		try {
			if (!scanner.hasNext()) {
				throw new WebServerRuntimeException("Error: Invalid parameter: '" + parameter + "'");
			}
			String key = scanner.next();

			String value = "";
			if (scanner.hasNext()) {
				try {
					value = URLDecoder.decode(scanner.next(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// won't get here
				}
			}

			parametersMap.put(key, value);

		} finally {
			scanner.close();
		}
	}

}
