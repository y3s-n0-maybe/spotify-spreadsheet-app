package org.j3lsmp.spotify_spreadsheet_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/api")
public class APIController {
	
	private static String clientID = "1f437741a3d14358a33953f99124058c";
	private static String clientSecret = "haha not this time bitches"; // remember to hide this
	
	private static String requestBody;
    
    private static Pattern tokenRegex = Pattern.compile("\\\"access_token\\\":\\\"([a-zA-Z0-9_-]+)\\\"");
    
	static {
		StringBuilder result = new StringBuilder();
		result.append(URLEncoder.encode("grant_type", StandardCharsets.UTF_8));
		result.append('=');
		result.append(URLEncoder.encode("client_credentials", StandardCharsets.UTF_8));

		requestBody = result.toString();
	}
	
	@GetMapping("/token")
    public ResponseEntity<String> getToken() throws IOException, URISyntaxException {
        System.out.println("Making api call for token");
        
        URL spotify = new URI("https://accounts.spotify.com/api/token").toURL();
        HttpURLConnection connection = (HttpURLConnection) spotify.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                (clientID + ":" + clientSecret).getBytes()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }
        
        int code = connection.getResponseCode();
        System.out.println(code);
        
        String response = "";
        try (InputStream inputStream = connection.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
                response += line;
            reader.close();
        }
        
        Matcher tokenMatcher = tokenRegex.matcher(response);
        
        boolean successful = tokenMatcher.find();
        
        if (successful) {
            String token = tokenMatcher.group(1);
            System.out.printf("Generated token: %s\n", token);
            return ResponseEntity.ok(token);
        } else
            return ResponseEntity.internalServerError().body("Token generation failed");
    }
	
	ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/playlist")
    public ResponseEntity<StreamingResponseBody> getSpreadsheet(@RequestBody String trackList) throws IOException {
        
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(trackList).get("tracks");
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet("Playlist");
        
        addHeaders(sheet);
        
        Iterator<JsonNode> iter = rootNode.elements();
        for (int i = 0; iter.hasNext(); i++) {
            JsonNode next = iter.next();
            XSSFRow trackRow = sheet.createRow(i);
            XSSFCell nameCell = trackRow.createCell(0);
            nameCell.setCellValue(next.get("track").get("name").toString());
            XSSFCell artistCell = trackRow.createCell(1);
            artistCell.setCellValue(artistString(next.get("track").get("artists")));
            XSSFCell albumCell = trackRow.createCell(2);
            albumCell.setCellValue(next.get("track").get("album").get("name").toString());
        }       

        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        
        new Thread(() -> {
            try {
	            book.write(pipedOutputStream);    
	            book.close();
	            pipedOutputStream.close();
            } catch (IOException e) {}
        }).start();
        
        StreamingResponseBody responseBody = outputStream -> {
        	byte[] buffer = new byte[1024];
        	int bytesRead;
        	while ((bytesRead = pipedInputStream.read(buffer)) != -1)
        		outputStream.write(buffer, 0, bytesRead);
        	pipedInputStream.close();
        };
        
        return ResponseEntity.ok()
        		.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"playlist.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
    
    String[] headers = {"Track Name", "Artist", "Album"};
    
    private void addHeaders(XSSFSheet sheet) {
    	XSSFRow row = sheet.createRow(0);
    	for (int i = 0; i < headers.length; i++) {
    		XSSFCell cell = row.createCell(i);
    		cell.setCellValue(headers[i]);
    	}
    }
    
    private String artistString(JsonNode artistsNode) {
    	StringBuilder sb = new StringBuilder();
    	Iterator<JsonNode> iter = ((ArrayNode) artistsNode).iterator();
    	while (iter.hasNext()) {
    		JsonNode artistNode = iter.next();
    		sb.append(artistNode.get("name").toString());
    		if (iter.hasNext())
    			sb.append(", ");
    	}
    	return sb.toString();
    }
}