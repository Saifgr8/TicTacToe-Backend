package com.example.TicTacToe.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonToString {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convertJsonToString(String[][] board){
        try {
           return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board", e);
        }
    }
}
