package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FoodTruckFinder {
    private static final String XML_DATA = "http://data.sfgov.org/resource/bbb8-hzi6.xml";
    private static final int DISPLAY_NUMBER = 10;
    private static final String USER_PROMPT = String.format("Press the enter key to continue with the next %d (or less)xx", DISPLAY_NUMBER);

    public static void main(String[] args) {
        Document doc = null;

        // Fetches the XML data and parses the data into a Document type.
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(XML_DATA);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(
                    result.toString().getBytes("UTF-8"));
            doc = builder.parse(input);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        NodeList list = doc.getElementsByTagName("row");

        List<FoodTruck> currentFoodTrucks = generateListOfFoodTrucks(list);

        // sort the food trucks by name
        Collections.sort(currentFoodTrucks);

        Scanner scanner = new Scanner(System.in);
        int count = 0;
        for(int i = 0; i < currentFoodTrucks.size(); i++) {
            if(count < DISPLAY_NUMBER) {
                System.out.println(currentFoodTrucks.get(i).toString());
                count++;
            }
            else {
                System.out.println(USER_PROMPT);
                scanner.nextLine();
                count = 0;
            }
            System.out.println();
        }
    }

    /**
     * This method generates a list of food trucks that are available at the current time
     * and current day of the week.
     *
     * @param list - list of nodes derived from each XML partition
     * @return the food trucks that are available at the current time and the current day of the week
     */
    private static List<FoodTruck> generateListOfFoodTrucks(NodeList list) {
        List<FoodTruck> currentFoodTrucks = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");

        int currentTime = Integer.parseInt(sdf.format(cal.getTime()));
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(cal.getTime());

        for(int temp = 0; temp < list.getLength(); temp++) {
            Node nNode = list.item(temp);

            Element eElement = (Element) nNode;
            FoodTruck foodTruck = new FoodTruck(eElement);

            // formatting the time into numbers for comparison
            int startTime = Integer.parseInt(foodTruck.getStartTime());
            int endTime = Integer.parseInt(foodTruck.getEndTime());

            if(currentTime > startTime && currentTime < endTime && dayOfWeek.equals(foodTruck.getDayOrder())) {
                currentFoodTrucks.add(foodTruck);
            }
        }

        return currentFoodTrucks;
    }
}

/**
 * This class is used to parse XML data into a Java object.
 */
class FoodTruck implements Comparable<FoodTruck>{
    private Element eElement;

    public FoodTruck(Element eElement) {
        this.eElement = eElement;
    }

    public String getDayOrder() {
        return eElement.getElementsByTagName("dayofweekstr").item(0).getTextContent();
    }

    public String getStartTime() {
        return eElement.getElementsByTagName("start24").item(0).getTextContent().replace(":", "");
    }

    public String getEndTime() {
        return eElement.getElementsByTagName("end24").item(0).getTextContent().replace(":", "");
    }

    public String getLocaton() {
        return eElement.getElementsByTagName("location").item(0).getTextContent();
    }

    public String getApplicant() {
        return eElement.getElementsByTagName("applicant").item(0).getTextContent();
    }

    public String toString() {
        return "Name: " + getApplicant() + ", Address: " + getLocaton();
    }

    @Override
    public int compareTo(FoodTruck o) {
        return getApplicant().compareTo(o.getApplicant());
    }
}
