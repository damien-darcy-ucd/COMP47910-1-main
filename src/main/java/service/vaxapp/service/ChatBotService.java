package service.vaxapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ChatBotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatBotService.class);

    private static final String url = "https://api.openai.com/v1/chat/completions";
    private static final String apiKeyNotSet = "Chatbot disabled. To enable, please set environment variable `openaiKey` to a valid OpenAI API key.";

    @Value("${openaiKey:#{null}}")
    private String openaiKey;

    @PostConstruct
    public void init() {
        if (openaiKey == null || openaiKey.isEmpty()) {
            logger.warn(apiKeyNotSet);
        }
    }

    private static final String model = "gpt-3.5-turbo";

    public String chatToBot(String prompt) {

        if (openaiKey == null || openaiKey.isEmpty()) {
            return apiKeyNotSet;
        }

        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + openaiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            StringBuffer response = new StringBuffer();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // calls the method to extract the message.
            return extractMessageFromJSONResponse(response.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractMessageFromJSONResponse(String response) {
        int start = response.indexOf("content")+ 11;

        int end = response.indexOf("\"", start);

        return response.substring(start, end);
    }
}
