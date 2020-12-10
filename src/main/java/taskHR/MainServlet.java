package taskHR;


import com.google.gson.stream.JsonReader;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class MainServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String params = req.getParameter("code");
        int foo;
        try {
            foo = Integer.parseInt(params);
        }
        catch (NumberFormatException e)
        {
            foo = 588409; //whatever, just showing about Tallinn
        }
        try {
            resp.getWriter().write(weatherCast(foo) + "\n"); //588409 Tallinn, 1006984 London

        } catch (APIException e) {
            e.printStackTrace();
            resp.getWriter().write("\n"+ "Ciy not Found" +"\n");
        }

        resp.getWriter().write("\n\nCity ID:\n" + foo + "\n\n" + "Show another city just add city ID to the end of URL" + "\n"
                + "e.g. http://localhost:8080/my-app/?code=1006984 //London");

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String params = req.getParameter("zip");
        resp.getWriter().write(searchTimeZone(params));
    }

    private String weatherCast(int name) throws APIException {
        OWM owm = new OWM("44b04c5c9801970e82bb155d508f5666");
        owm.setUnit(OWM.Unit.METRIC);
        CurrentWeather cwd = owm.currentWeatherByCityId(name);
        return "City: " + cwd.getCityName() + "\n" + "Temperature: " + cwd.getMainData().getTempMax()
                + "/" + cwd.getMainData().getTempMin() + "\'C" + "\n" + "Pressure: " + cwd.getMainData().getPressure() + " hPa"
                + "\n" + "Humidity: " + cwd.getMainData().getHumidity() + " %"
                + "\n" + "Wind: " + cwd.getWindData().getSpeed() + " m/s";
    }

    private String searchTimeZone(String request) {
        String result = "";
        if (request == null || request.equals("")) {
            result = "Input is empty!";
            return result;
        }
        String jsonPath = "https://www.zipcodeapi.com/rest/An4UrDOAosu7lwfelRW1qTzXrYeaZ6n1ahwJqmPx74s2qLayMsMqeA7pzUqguYC2/info.json/"+request+"/degrees";

        try (JsonReader jsonReader = new JsonReader(
                new InputStreamReader(
                        new URL(jsonPath).openStream()))) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                    if (jsonReader.nextName().equals("timezone")) {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            if (jsonReader.nextName().equals("timezone_identifier")) {
                                result += jsonReader.nextString();
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    } else {
                        jsonReader.skipValue();
                    }
            }
            jsonReader.endObject();
    } catch (IOException e) {
            e.printStackTrace();
            result = "A zip code you provided was not found.";
        }
        return result;
    }
}
