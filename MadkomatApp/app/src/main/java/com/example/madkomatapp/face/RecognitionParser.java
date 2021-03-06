package com.example.madkomatapp.face;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecognitionParser {
    public static List<Face> extractFaces(String doc) {
        List<Face> faces = new ArrayList<>();

        try {
            JSONObject reader = new JSONObject(doc);
            JSONObject response = reader.getJSONObject("Response");
            JSONArray faceDetails = response.getJSONArray("FaceDetails");

            for(int i=0; i<faceDetails.length(); ++i) {
                JSONObject faceDetail = faceDetails.getJSONObject(i);
                JSONObject boundingBox = faceDetail.getJSONObject("BoundingBox");
                JSONObject ageRange = faceDetail.getJSONObject("AgeRange");
                JSONObject smile = faceDetail.getJSONObject("Smile");

                faces.add(new FaceBuilder()
                        .setWidth(boundingBox.getDouble("Width"))
                        .setHeight(boundingBox.getDouble("Height"))
                        .setLeft(boundingBox.getDouble("Left"))
                        .setTop(boundingBox.getDouble("Top"))
                        .setAgeRangeLow(ageRange.getInt("Low"))
                        .setAgeRangeHigh(ageRange.getInt("High"))
                        .setSmiling(smile.getBoolean("Value"))
                        .setSmilingConfidence(smile.getDouble("Confidence"))
                        .createFace());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Collections.unmodifiableList(faces);
    }
}
