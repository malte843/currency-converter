package de.malte;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class GuiMainMenu extends JFrame implements ActionListener {
    private JPanel panel1;
    private JTextField txtResult;
    private JComboBox cbInput;
    private JComboBox cbOutput;
    private JButton btnConvert;
    private JButton btnRefresh;
    private JSpinner spAmount;
    private ArrayList<String> currencyList = new ArrayList<>(); // bsp: 102::Euro::EUR


    public void refreshMenu() {
        try {
            JsonObject jObj = httpRequest("https://api.exchangerate.host/symbols");
            String currenciesStr = jObj.get("symbols").toString();
            JsonElement element = JsonParser.parseString(currenciesStr);
            JsonObject object = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
            int index = 0;
            for (Map.Entry<String, JsonElement> entry : entries) {
                String symbol = entry.getKey();
                String name = object.get(symbol).getAsJsonObject().get("description").getAsString();
                String str = index + "::" + name + "::" + symbol;
                currencyList.add(str);

                //System.out.println(str);
                index++;
            }
            fillCombos();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public GuiMainMenu() {
        setContentPane(panel1);
        setTitle("Currency Converter");
        setSize(610, 180);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        refreshMenu();
        setVisible(true);
        setResizable(false);
        btnConvert.addActionListener(this);
        btnRefresh.addActionListener(this);
    }

    public static void main(String[] args) {
        GuiMainMenu mainMenu = new GuiMainMenu();
    }

    public void convertCurrency() {
        String input = cbInput.getSelectedItem().toString();
        String output = cbOutput.getSelectedItem().toString();
        String sbIn = getSymbolOf(input);
        String sbOut = getSymbolOf(output);

        String amount = spAmount.getValue().toString();
        //System.out.println(amount + " " + sbIn + " to " + sbOut);

        if (amount.equalsIgnoreCase("0")) {
            txtResult.setText("0");
            return;
        }

        String url = "https://api.exchangerate.host/convert?from=" + sbIn + "&to=" + sbOut + "&amount=" + amount;

        try {
            String result = httpRequest(url).get("result").getAsString();
            txtResult.setText(result);
        } catch (IOException e) {
            System.out.println(e);
            txtResult.setText(e.getMessage());
        }
    }

    private int getIndexOf(String currency) {
        String[] ccc = currency.split("::");
        return Integer.parseInt(ccc[0]);
    }

    private String getNameOf(String currency) {
        String[] ccc = currency.split("::");
        return ccc[1];
    }

    private String getSymbolOf(String currency) {
        //String[] ccc = currency.split("::");
        //return ccc[2];
        return currency.split("\\(")[1].replace(")", "");
    }

    public void fillCombos() {
        for (String currency : currencyList) {
            String[] ccc = currency.split("::");
            int index = Integer.parseInt(ccc[0]);
            String name = ccc[1];
            String symbol = ccc[2];
            Object obj = name + " ("+symbol+")";
            cbInput.addItem(obj);
            cbOutput.addItem(obj);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnConvert) {
            convertCurrency();
        } else if (e.getSource() == btnRefresh) {
            refreshMenu();
        }
    }

    private JsonObject httpRequest(String url_str) throws IOException {
        URL url = new URL(url_str);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        return root.getAsJsonObject();
    }
}
