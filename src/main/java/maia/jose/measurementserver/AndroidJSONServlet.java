package maia.jose.measurementserver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import maia.jose.measurementserver.jsonbeans.TemporaryParse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class AndroidJSONServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String json = "";
        StringBuilder builder = new StringBuilder();
        for (String line = null; (line = br.readLine()) != null;) {
            builder.append(line).append("\n");
        }
        json = builder.toString();

        ObjectMapper mapper = new ObjectMapper();
        //mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        TemporaryParse jsonDestination;

        jsonDestination = mapper.readValue(json.getBytes(),TemporaryParse.class);

        MeasurementProcessor processor = new MeasurementProcessor(jsonDestination);
        System.out.println("Got a "+processor.getTypeString()+" measurement obtained at "+jsonDestination.getTimestamp());

        PrintWriter writer = resp.getWriter();
        try {
            processor.process();
            writer.write("Measurement successfully received!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
