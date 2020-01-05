package com.example.madkomatapp.face;

import com.amazonaws.util.IOUtils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;


public class RecognitionParserTest {

    @Ignore
    @Test
    public void extractFaces() throws IOException {
        String doc = IOUtils.toString(Objects.requireNonNull(getClass().getClassLoader())
                .getResourceAsStream("response.json"));

        Assert.assertEquals(3, RecognitionParser.extractFaces(doc).size());
    }
}