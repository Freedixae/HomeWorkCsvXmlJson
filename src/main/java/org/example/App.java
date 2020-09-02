package org.example;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) {
        JSONObject object = new JSONObject();
        String xml = "data.xml";
        String csv = "data.csv";
//      создаем файл csv
        newCSV(csv);
//      из csv в json
        List<Person> listCsv = parseCsv(csv);
        object.put("PersonCSV", stringJson(listCsv));
        writeString(object, "csvdata.json");
//      из xml в json
        List<Person> listXml = parseXML(xml);
        object.clear();
        object.put("PersonXML", stringJson(listXml));
        writeString(object, "xmldata.json");
//      чтение и преобразование в класс файла .json
        String json = readString("csvdata.json");
        System.out.println(jsonToPerson(json));
    }

    public static List<Person> parseCsv(String name) {
        List<Person> personList = new ArrayList<>();
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        try (CSVReader csvReader = new CSVReader(new FileReader(name));) {
            ColumnPositionMappingStrategy<Person> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Person.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Person> csv = new CsvToBeanBuilder<Person>(csvReader).
                    withMappingStrategy(strategy).
                    build();
            personList = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return personList;
    }

    public static List<Person> parseXML(String name) {
        List<Person> personList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(name);
            Node root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;
                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                    Person person = new Person(id, firstName, lastName, country, age);
                    personList.add(person);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return personList;
    }

    public static void newCSV(String name) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(name))) {
            String[] record = "1,Smith,John,USA,25".split(",");
            String[] record2 = "2,Ivan,Petrov,RU,23".split(",");
            writer.writeNext(record);
            writer.writeNext(record2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String stringJson(List<Person> list) {
        Type listType = new TypeToken<List<Person>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(list, listType);
    }

    public static void writeString(JSONObject obj, String name) {
        try (FileWriter file = new FileWriter(name)) {
            file.write(obj.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readString(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONParser parser = new JSONParser();
        String str = "";
        try {
            Object obj = parser.parse(new FileReader(s));
            JSONObject jsonObject = (JSONObject) obj;
            String person = (String) jsonObject.get("PersonCSV");
            stringBuilder.append(person);
            str += stringBuilder;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static List<Person> jsonToPerson(String s) {
        List<Person> personList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(s);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                personList.add(gson.fromJson(String.valueOf(jsonObject), Person.class));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return personList;
    }
}
