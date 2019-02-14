package com.bebound.template.controller;

import com.bebound.template.response.Failure;
import com.bebound.template.model.Request;
import com.bebound.template.response.Response;
import com.bebound.template.response.Success;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class TemplateController {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * We are listening for HTTP request, with JSON
     * First, we check mandatory fields
     * Then, we process the parameters from the JSON object
     * Finally, we send the response, with the correct parameters
     * @param headers
     * @param body
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<String> check(@RequestHeader Map<String, String> headers, @RequestBody String body) {
        // You may want to check authorization here, request should have Basic auth header
        JsonObject jsonRequestObject = new JsonParser().parse(body).getAsJsonObject();
        if (!jsonRequestObject.has("moduleName"))
            return new ResponseEntity<>("moduleName missing", HttpStatus.BAD_REQUEST);

        if (!jsonRequestObject.has("moduleId"))
            return new ResponseEntity<>("moduleId missing", HttpStatus.BAD_REQUEST);

        if (!jsonRequestObject.has("moduleVersion"))
            return new ResponseEntity<>("moduleVersion missing", HttpStatus.BAD_REQUEST);

        if (!jsonRequestObject.has("operation"))
            return new ResponseEntity<>("operation missing", HttpStatus.BAD_REQUEST);

        if (!jsonRequestObject.has("params"))
            return new ResponseEntity<>("params missing", HttpStatus.BAD_REQUEST);

        if (!jsonRequestObject.has("transport"))
            return new ResponseEntity<>("transport missing", HttpStatus.BAD_REQUEST);

        Request.MapRequest request = gson.fromJson(body, Request.MapRequest.class);
        request.setJsonParams(gson.toJson(request.getParameters()));

        try {
            return new ResponseEntity<>(gson.toJson(performRequest(request)), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * We perform the request:
     * 1- We check if operation name from Be-App Manifest is equal to the expected operation name
     * 2- If ok, we create a success response with the parameter "length", as defined in our Be-App Manifest
     * 3- If ko, we create a failure response with a dedicated message
     * @param request from the JSON we parsed
     * @return the response
     */
    private Response performRequest(Request request) {
        Logger.getAnonymousLogger().info("Operation name: " + request.getOperationName());

        if ("get_character".equals(request.getOperationName())) {
            Logger.getAnonymousLogger().info("PERFORM REQUEST TO BE-BOUND SERVER");
            Request.MapRequest a = (Request.MapRequest) request;
            Logger.getAnonymousLogger().info(a.getJsonParams());

            int id = Float.valueOf(request.getParameters().get("id").toString()).intValue();

            OkHttpClient client = new OkHttpClient();
            String url = String.format(Locale.ENGLISH, "https://swapi.co/api/people/%d/", id);
            com.squareup.okhttp.Request requestHttpChar = new com.squareup.okhttp.Request.Builder()
                    .url(url)
                    .build();

            try {
                com.squareup.okhttp.Response responseChar = client.newCall(requestHttpChar).execute();
                String jsonChar = responseChar.body().string();
                JsonObject objChar = new JsonParser().parse(jsonChar).getAsJsonObject();
                String charName = objChar.getAsJsonPrimitive("name").getAsString();
                String homeWorldUrl = objChar.getAsJsonPrimitive("homeworld").getAsString();
                com.squareup.okhttp.Request requestHttpPlanet = new com.squareup.okhttp.Request.Builder()
                                .url(homeWorldUrl)
                                .build();
                com.squareup.okhttp.Response responsePlanet = client.newCall(requestHttpPlanet).execute();
                String jsonPlanet = responsePlanet.body().string();
                JsonObject objPlanet = new JsonParser().parse(jsonPlanet).getAsJsonObject();
                String planetName = objPlanet.getAsJsonPrimitive("name").getAsString();
                Logger.getAnonymousLogger().info("char_name = " + charName + " planet_name = " + planetName);

                return new Success.Builder()
                        .withParameter("char_name", charName)
                        .withParameter("planet_name", planetName)
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
                return new Failure.Builder().withErrorStatus("This operation: " + request.getOperationName() + " didn't work").build();
            }
        }

        Logger.getAnonymousLogger().info("ERROR REQUEST");

        return new Failure.Builder().withErrorStatus("This operation: " + request.getOperationName() + " doesn't exist").build();
    }
}
