package io.github.redpanda4552.HifumiBot.util;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPT {

    private static final String CHAT_GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final int CHAT_GPT_RPM = 20;
    private static final int CHAT_GPT_TPM = 40000;

    private HashMap<Instant, Integer> requestHistory;
    private int tokens;

    public ChatGPT() {
        this.requestHistory = new HashMap<Instant, Integer>();
        this.tokens = 0;
    }

    public synchronized String translate(String userId, String str) {
        return translate(userId, str, "english");
    }

    public synchronized String translate(String userId, String str, String lang) {
        this.cleanupHistory();

        if (HifumiBot.getChatGptToken() == null || this.requestHistory.size() >= CHAT_GPT_RPM || this.tokens >= CHAT_GPT_TPM) {
            return null;
        }

        RequestTemplate template = new RequestTemplate(userId);
        template.messages.add(new RequestTemplateMessage(str, lang));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String bodyStr = gson.toJson(template);
        Builder builder = new Request.Builder()
            .url(CHAT_GPT_ENDPOINT)
            .header("Authorization", "Bearer " + HifumiBot.getChatGptToken())
            .header("Content-Type", "application/json")
            .post(RequestBody.create(bodyStr, MediaType.parse("application/json")));
        
        Request req = builder.build();
        
        try {
            Response res = HifumiBot.getSelf().getHttpClient().newCall(req).execute();
            
            if (res.isSuccessful()) {
                ResponseTemplate resObj = (ResponseTemplate) gson.fromJson(res.body().string(), ResponseTemplate.class);
                this.requestHistory.put(Instant.now(), resObj.usage.total_tokens);
                return resObj.choices.get(0).message.content;
            } else {
                return null;
            }
        } catch (IOException e) {
            Messaging.logException("ChatGPT", "translate", e);
        } catch (Exception e) {
            Messaging.logException("ChatGPT", "translate", e);
        }
        
        return null;
    }

    private void cleanupHistory() {
        Instant now = Instant.now();
        this.tokens = 0;
        this.requestHistory.entrySet().removeIf(entry -> (entry.getKey().plusSeconds(60).isBefore(now)));
        
        for (Instant instant : this.requestHistory.keySet()) {
            this.tokens += this.requestHistory.get(instant);
        }
    }

    public class RequestTemplate {
        public String model;
        public ArrayList<RequestTemplateMessage> messages;
        public String user;

        public RequestTemplate(String userId) {
            this.model = "gpt-3.5-turbo";
            this.messages = new ArrayList<RequestTemplateMessage>();
            this.user = userId;
        }
    }

    public class RequestTemplateMessage {
        public String role = "user";
        public String content = "Translate the following to ";

        public RequestTemplateMessage(String content, String lang) {
            this.content += lang + ", then identify the source language on a new line: " + content;
        }
    }

    public class ResponseTemplate {
        public String id;
        public String object;
        public long created;
        public String model;
        public ResponseTemplateUsage usage;
        public ArrayList<ResponseTemplateChoice> choices;
    }

    public class ResponseTemplateUsage {
        public int prompt_tokens;
        public int completion_tokens;
        public int total_tokens;
    }

    public class ResponseTemplateChoice {
        public ResponseTemplateChoiceMessage message;
        public String finish_reason;
        public int index;
    }

    public class ResponseTemplateChoiceMessage {
        public String role;
        public String content;
    }
}
