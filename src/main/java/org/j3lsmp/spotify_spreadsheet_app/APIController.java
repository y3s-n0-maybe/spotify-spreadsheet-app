package org.j3lsmp.spotify_spreadsheet_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class APIController {
	
	private static String clientID = "1f437741a3d14358a33953f99124058c";
	private static String clientSecret = "8e4da50a4b494781b8e72d71ea61be6c";
	
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
    public ResponseEntity<String> getToken() throws IOException {
        System.out.println("Making api call for token");
        
        URL spotify = new URL("https://accounts.spotify.com/api/token");
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
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
        }
        
        Matcher tokenMatcher = tokenRegex.matcher(response);
        
        boolean successful = tokenMatcher.find();
        
        if (successful) {
            String token = tokenMatcher.group(1);
            System.out.printf("Generated token: %s\n", token);
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.internalServerError().body("Token generation failed");
        }
    }
}