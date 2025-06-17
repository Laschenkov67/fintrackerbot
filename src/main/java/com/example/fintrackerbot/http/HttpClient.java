package com.example.fintrackerbot.http;

import java.io.IOException;

public interface HttpClient {
    String get(String url, String token) throws IOException;
}