package aqimonitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

public class AQIMonitor2GUI extends JFrame {

    private static final String AIRNOW_API_KEY = "D2804F1D-E3E5-4430-8B4B-20F889CC4958";
    private static final String HISTORICAL_DATA_FILE = "AQI_Data_Mock.csv";
    private static final java.util.List<String> SUPPORTED_HISTORICAL_CITIES = java.util.Arrays.asList(
            "New York", "Los Angeles", "Chicago", "Houston"
    );
    private static final java.util.Map<String, String> CITY_STATES = java.util.Map.of(
            "New York", "New York",
            "Los Angeles", "California",
            "Chicago", "Illinois",
            "Houston", "Texas"
    );
    private static final Color AIRNOW_BLUE = new Color(23, 94, 181);

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    public AQIMonitor2GUI() {
        setTitle("AQIMonitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 950);
        setResizable(false);
        setLocationRelativeTo(null);

        setContentPane(new GradientPanel());

        buildWelcomePage();
        buildCurrentPage();
        buildHistoricalPage();
        mainPanel.setOpaque(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private void buildWelcomePage() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);

        JLabel title = new JLabel("Welcome to AQIMonitor", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 54));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(80, 0, 0, 0));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 80, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(120, 400, 200, 400));
        JButton btnCurrent = new JButton("<html><div style='text-align:center;'>Current<br>AQI Data</div></html>");
        btnCurrent.setFont(new Font("Times New Roman", Font.BOLD, 32));
        btnCurrent.setBackground(Color.WHITE);
        btnCurrent.setForeground(AIRNOW_BLUE);
        btnCurrent.setFocusPainted(false);
        btnCurrent.setPreferredSize(new Dimension(300, 200));

        JButton btnHistorical = new JButton("<html><div style='text-align:center;'>Historical<br>AQI Data</div></html>");
        btnHistorical.setFont(new Font("Times New Roman", Font.BOLD, 32));
        btnHistorical.setBackground(Color.WHITE);
        btnHistorical.setForeground(AIRNOW_BLUE);
        btnHistorical.setFocusPainted(false);
        btnHistorical.setPreferredSize(new Dimension(300, 200));

        btnCurrent.addActionListener(e -> cardLayout.show(mainPanel, "current"));
        btnHistorical.addActionListener(e -> cardLayout.show(mainPanel, "historical"));

        btnPanel.add(btnCurrent);
        btnPanel.add(btnHistorical);

        JLabel footer = new JLabel("<html><div style='color:white;text-align:center;font-size:18pt;padding:10px;'>"
                + "Inspired by <b>AirNow.gov</b>"
                + "</div></html>", SwingConstants.CENTER);
        footer.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        footer.setBorder(new EmptyBorder(0, 0, 50, 0));

        welcomePanel.add(title, BorderLayout.NORTH);
        welcomePanel.add(btnPanel, BorderLayout.CENTER);
        welcomePanel.add(footer, BorderLayout.SOUTH);

        mainPanel.add(welcomePanel, "welcome");
        cardLayout.show(mainPanel, "welcome");
    }

    private void buildCurrentPage() {
        JPanel currentPanel = new JPanel(new BorderLayout());
        currentPanel.setOpaque(false);

        JLabel title = new JLabel("Current Air Quality Index", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(40, 0, 10, 0));

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JLabel cityLabel = new JLabel("City:");
        cityLabel.setFont(new Font("Times New Roman", Font.PLAIN, 28));
        cityLabel.setForeground(Color.WHITE);
        JTextField cityField = new JTextField(18);
        cityField.setFont(new Font("Times New Roman", Font.PLAIN, 28));
        JButton getBtn = new JButton("Get AQI Data");
        getBtn.setFont(new Font("Times New Roman", Font.BOLD, 28));
        getBtn.setBackground(Color.WHITE);
        getBtn.setForeground(AIRNOW_BLUE);
        getBtn.setFocusPainted(false);

        formPanel.add(cityLabel);
        formPanel.add(cityField);
        formPanel.add(getBtn);

        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);

        JLabel desc = new JLabel("<html><div style='text-align:center;color:white;font-size:17pt;'>"
                + "AQIMonitor fetches real-time data from the AirNow API. <br>"
                + "Data includes <b>PM2.5</b> and <b>Ozone (O3)</b> parameters where available."
                + "</div></html>", SwingConstants.CENTER);
        desc.setBorder(new EmptyBorder(10,0,0,0));
        desc.setFont(new Font("Times New Roman", Font.PLAIN, 24));

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Times New Roman", Font.BOLD, 22));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(AIRNOW_BLUE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BorderLayout());
        centerContent.add(formPanel, BorderLayout.NORTH);
        centerContent.add(resultPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setLayout(new BorderLayout());
        southPanel.add(desc, BorderLayout.CENTER);
        southPanel.add(backBtn, BorderLayout.SOUTH);

        currentPanel.add(title, BorderLayout.NORTH);
        currentPanel.add(centerContent, BorderLayout.CENTER);
        currentPanel.add(southPanel, BorderLayout.SOUTH);

        getBtn.addActionListener(e -> {
            String city = cityField.getText().trim();
            resultPanel.removeAll();
            resultPanel.revalidate();
            resultPanel.repaint();

            if (city.isEmpty()) {
                showError(resultPanel, "Please enter a city name.");
                return;
            }

            double[] coords = getCoordinates(city);
            if (coords == null) {
                showError(resultPanel, "Could not find coordinates for " + city + ".");
                return;
            }
            double lat = coords[0], lon = coords[1];

            JSONArray results = fetchCurrentAQIFromAirNow(lat, lon);
            if (results == null || results.length() == 0) {
                showError(resultPanel, "No AQI data available for this city right now.");
                return;
            }
            // Find both PM2.5 and O3, or null if not present
            JSONObject pm25Obj = null, o3Obj = null;
            for (int i = 0; i < results.length(); i++) {
                JSONObject entry = results.getJSONObject(i);
                String param = entry.optString("ParameterName", "");
                if (param.equalsIgnoreCase("PM2.5")) pm25Obj = entry;
                if (param.equalsIgnoreCase("O3")) o3Obj = entry;
            }
            JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 60, 0));
            chartsPanel.setOpaque(false);

            AQIGaugeChart pm25Chart = new AQIGaugeChart(
                pm25Obj != null ? pm25Obj.optInt("AQI", -1) : -1,
                pm25Obj != null ? pm25Obj.has("Category") ? pm25Obj.getJSONObject("Category").optString("Name", "") : "" : "",
                "PM2.5"
            );
            AQIGaugeChart o3Chart = new AQIGaugeChart(
                o3Obj != null ? o3Obj.optInt("AQI", -1) : -1,
                o3Obj != null ? o3Obj.has("Category") ? o3Obj.getJSONObject("Category").optString("Name", "") : "" : "",
                "Ozone (O3)"
            );
            chartsPanel.add(pm25Chart);
            chartsPanel.add(o3Chart);

            JPanel cityCoordsPanel = new JPanel();
            cityCoordsPanel.setOpaque(false);
            cityCoordsPanel.setLayout(new BoxLayout(cityCoordsPanel, BoxLayout.Y_AXIS));
            JLabel cityL = new JLabel("City: " + city, SwingConstants.CENTER);
            cityL.setFont(new Font("Times New Roman", Font.BOLD, 28));
            cityL.setForeground(Color.WHITE);
            JLabel coordL = new JLabel(String.format("Coordinates: %.4f, %.4f", lat, lon), SwingConstants.CENTER);
            coordL.setFont(new Font("Times New Roman", Font.BOLD, 24));
            coordL.setForeground(Color.WHITE);
            cityL.setAlignmentX(Component.CENTER_ALIGNMENT);
            coordL.setAlignmentX(Component.CENTER_ALIGNMENT);
            cityCoordsPanel.add(cityL);
            cityCoordsPanel.add(coordL);

            JPanel infoBelowPanel = new JPanel(new GridLayout(1, 2, 60, 0));
            infoBelowPanel.setOpaque(false);
            JLabel pm25Info = new JLabel(
                "<html><div style='color:white;font-size:24pt; text-align:center;'><b>PM2.5 AQI:</b> "
                        + (pm25Obj != null ? pm25Obj.optInt("AQI", -1) : "N/A")
                        + "<br><b>Category:</b> "
                        + (pm25Obj != null && pm25Obj.has("Category") ? pm25Obj.getJSONObject("Category").optString("Name", "") : "N/A")
                        + "</div></html>", SwingConstants.CENTER);
            pm25Info.setFont(new Font("Times New Roman", Font.BOLD, 26));
            JLabel o3Info = new JLabel(
                "<html><div style='color:white;font-size:24pt; text-align:center;'><b>Ozone (O3) AQI:</b> "
                        + (o3Obj != null ? o3Obj.optInt("AQI", -1) : "N/A")
                        + "<br><b>Category:</b> "
                        + (o3Obj != null && o3Obj.has("Category") ? o3Obj.getJSONObject("Category").optString("Name", "") : "N/A")
                        + "</div></html>", SwingConstants.CENTER);
            o3Info.setFont(new Font("Times New Roman", Font.BOLD, 26));
            infoBelowPanel.add(pm25Info);
            infoBelowPanel.add(o3Info);

            JPanel fullCenterPanel = new JPanel();
            fullCenterPanel.setOpaque(false);
            fullCenterPanel.setLayout(new BoxLayout(fullCenterPanel, BoxLayout.Y_AXIS));
            fullCenterPanel.add(Box.createVerticalStrut(18));
            fullCenterPanel.add(cityCoordsPanel);
            fullCenterPanel.add(Box.createVerticalStrut(18));
            fullCenterPanel.add(chartsPanel);
            fullCenterPanel.add(Box.createVerticalStrut(18));
            fullCenterPanel.add(infoBelowPanel);

            resultPanel.setLayout(new BorderLayout());
            resultPanel.add(fullCenterPanel, BorderLayout.CENTER);
            resultPanel.revalidate();
            resultPanel.repaint();
        });

        mainPanel.add(currentPanel, "current");
    }

    private void buildHistoricalPage() {
        JPanel historicalPanel = new JPanel(new BorderLayout());
        historicalPanel.setOpaque(false);

        // Info panel at the top
        StringBuilder citiesList = new StringBuilder();
        for (String c : SUPPORTED_HISTORICAL_CITIES) {
            citiesList.append("• ").append(c).append(", ").append(CITY_STATES.get(c)).append("<br>");
        }
        JLabel info = new JLabel("<html><div style='font-size:20pt;color:white;margin:10px 0;text-align:center;'>"
                + "<b>AQI Monitor holds historical data for the 4 largest (population-wise) cities in the United States:</b><br>"
                + citiesList
                + "<br>Historical data starts from <b>June 1, 2025</b>.<br><br>"
                + "You can select an <b>exact time</b> (e.g. June 9, 2025 at 02:00) or an <b>interval</b> (e.g. AQI data from June 1st at 02:00 until June 2nd at 3 pm).<br>"
                + "For an interval, AQIMonitor will show a graph for both PM2.5 and O3.<br>"
                + "</div></html>", SwingConstants.CENTER);
        info.setFont(new Font("Times New Roman", Font.PLAIN, 26));
        info.setBorder(new EmptyBorder(0, 30, 0, 30));

        // GridBagLayout for correct visibility and alignment
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 14, 6, 14);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy = 0; gbc.gridx = 0;

        // ComboBox with initial empty selection
        JLabel cityLabel = new JLabel("City:");
        cityLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        cityLabel.setForeground(Color.WHITE);

        String[] cityOptions = new String[SUPPORTED_HISTORICAL_CITIES.size() + 1];
        cityOptions[0] = "";
        for (int i = 0; i < SUPPORTED_HISTORICAL_CITIES.size(); ++i)
            cityOptions[i+1] = SUPPORTED_HISTORICAL_CITIES.get(i);
        JComboBox<String> cityCombo = new JComboBox<>(cityOptions);
        cityCombo.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        cityCombo.setSelectedIndex(0);

        JLabel typeLabel = new JLabel("Data Type:");
        typeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        typeLabel.setForeground(Color.WHITE);

        String[] typeOptions = {"", "Exact Time", "Interval"};
        JComboBox<String> typeCombo = new JComboBox<>(typeOptions);
        typeCombo.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        typeCombo.setSelectedIndex(0);

        JLabel dateLabel1 = new JLabel("Date (YYYY-MM-DD):");
        dateLabel1.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        dateLabel1.setForeground(Color.WHITE);
        JTextField dateField1 = new JTextField(10);
        dateField1.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        JLabel hourLabel1 = new JLabel("Hour (0-23):");
        hourLabel1.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        hourLabel1.setForeground(Color.WHITE);
        JTextField hourField1 = new JTextField(2);
        hourField1.setFont(new Font("Times New Roman", Font.PLAIN, 22));

        JLabel dateLabel2 = new JLabel("End Date (YYYY-MM-DD):");
        dateLabel2.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        dateLabel2.setForeground(Color.WHITE);
        JTextField dateField2 = new JTextField(10);
        dateField2.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        JLabel hourLabel2 = new JLabel("End Hour (0-23):");
        hourLabel2.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        hourLabel2.setForeground(Color.WHITE);
        JTextField hourField2 = new JTextField(2);
        hourField2.setFont(new Font("Times New Roman", Font.PLAIN, 22));

        JButton getBtn = new JButton("Get AQI Data");
        getBtn.setFont(new Font("Times New Roman", Font.BOLD, 28));
        getBtn.setBackground(Color.WHITE);
        getBtn.setForeground(AIRNOW_BLUE);
        getBtn.setFocusPainted(false);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(cityLabel, gbc);
        gbc.gridx = 1; formPanel.add(cityCombo, gbc);
        gbc.gridx = 2; formPanel.add(typeLabel, gbc);
        gbc.gridx = 3; formPanel.add(typeCombo, gbc);
        // Row 2
        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(dateLabel1, gbc);
        gbc.gridx = 1; formPanel.add(dateField1, gbc);
        gbc.gridx = 2; formPanel.add(hourLabel1, gbc);
        gbc.gridx = 3; formPanel.add(hourField1, gbc);
        // Row 3 (interval only)
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(dateLabel2, gbc);
        gbc.gridx = 1; formPanel.add(dateField2, gbc);
        gbc.gridx = 2; formPanel.add(hourLabel2, gbc);
        gbc.gridx = 3; formPanel.add(hourField2, gbc);
        // Row 4 (button)
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(getBtn, gbc);

        // Hide interval fields initially
        dateLabel2.setVisible(false);
        dateField2.setVisible(false);
        hourLabel2.setVisible(false);
        hourField2.setVisible(false);

        typeCombo.addActionListener(e -> {
            boolean interval = typeCombo.getSelectedIndex() == 2;
            dateLabel2.setVisible(interval);
            dateField2.setVisible(interval);
            hourLabel2.setVisible(interval);
            hourField2.setVisible(interval);
            formPanel.revalidate();
            formPanel.repaint();
        });

        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);

        JLabel desc = new JLabel("<html><div style='text-align:center;color:white;font-size:17pt;'>"
                + "Historical data is fetched from <b>AQI_Data_Mock.csv</b>, a mock dataset of <b>PM2.5</b> and <b>O3</b> parameters.<br>"
                + "</div></html>", SwingConstants.CENTER);
        desc.setBorder(new EmptyBorder(14,0,0,0));
        desc.setFont(new Font("Times New Roman", Font.PLAIN, 24));

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Times New Roman", Font.BOLD, 22));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(AIRNOW_BLUE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BorderLayout());
        centerContent.add(formPanel, BorderLayout.NORTH);
        centerContent.add(resultPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setLayout(new BorderLayout());
        southPanel.add(desc, BorderLayout.CENTER);
        southPanel.add(backBtn, BorderLayout.SOUTH);

        historicalPanel.add(info, BorderLayout.NORTH);
        historicalPanel.add(centerContent, BorderLayout.CENTER);
        historicalPanel.add(southPanel, BorderLayout.SOUTH);

        getBtn.addActionListener(e -> {
            String city = (String) cityCombo.getSelectedItem();
            String type = (String) typeCombo.getSelectedItem();
            String date1 = dateField1.getText().trim();
            String hour1 = hourField1.getText().trim();
            resultPanel.removeAll();

            if (city == null || city.isEmpty() || type == null || type.isEmpty() || date1.isEmpty() || hour1.isEmpty()) {
                showError(resultPanel, "Please fill in all required fields.");
                return;
            }

            if (type.equals("Exact Time")) {
                LocalDate date;
                int hour;
                try {
                    date = LocalDate.parse(date1, DateTimeFormatter.ISO_LOCAL_DATE);
                    hour = Integer.parseInt(hour1);
                    if (hour < 0 || hour > 23) throw new NumberFormatException();
                } catch (Exception ex) {
                    showError(resultPanel, "Invalid date or hour format.");
                    return;
                }
                java.util.List<String[]> rows = getHistoricalRows(city, date, hour);
                if (rows.isEmpty()) {
                    showError(resultPanel, "No historical AQI data found for this time.");
                } else {
                    String pm25 = null, pm25cat = null, o3 = null, o3cat = null;
                    for (String[] arr : rows) {
                        if (arr[4].equalsIgnoreCase("PM2.5")) {
                            pm25 = arr[5];
                            pm25cat = arr[6];
                        } else if (arr[4].equalsIgnoreCase("O3")) {
                            o3 = arr[5];
                            o3cat = arr[6];
                        }
                    }
                    JPanel textPanel = new JPanel();
                    textPanel.setOpaque(false);
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                    JLabel topLabel = new JLabel(
                        "<html><div style='color:white; font-size:26pt; text-align:center;'>"
                        + "<b>City:</b> " + city + "<br>"
                        + "<b>Date:</b> " + date.toString() + " <b>Hour:</b> " + String.format("%02d", hour)
                        + "</div></html>");
                    topLabel.setFont(new Font("Times New Roman", Font.BOLD, 28));
                    topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    textPanel.add(topLabel);

                    if (pm25 != null) {
                        JLabel pm25Label = new JLabel(
                            "<html><div style='color:white; font-size:24pt; text-align:center;'>"
                            + "PM2.5 AQI: <b>" + pm25 + "</b> | Category: " + pm25cat + "</div></html>");
                        pm25Label.setFont(new Font("Times New Roman", Font.BOLD, 26));
                        pm25Label.setAlignmentX(Component.CENTER_ALIGNMENT);
                        textPanel.add(pm25Label);
                    }
                    if (o3 != null) {
                        JLabel o3Label = new JLabel(
                            "<html><div style='color:white; font-size:24pt; text-align:center;'>"
                            + "O3 AQI: <b>" + o3 + "</b> | Category: " + o3cat + "</div></html>");
                        o3Label.setFont(new Font("Times New Roman", Font.BOLD, 26));
                        o3Label.setAlignmentX(Component.CENTER_ALIGNMENT);
                        textPanel.add(o3Label);
                    }
                    resultPanel.add(textPanel);
                }
            } else {
                String date2 = dateField2.getText().trim();
                String hour2 = hourField2.getText().trim();
                LocalDate sd, ed;
                int sh, eh;
                try {
                    sd = LocalDate.parse(date1, DateTimeFormatter.ISO_LOCAL_DATE);
                    sh = Integer.parseInt(hour1);
                    ed = LocalDate.parse(date2, DateTimeFormatter.ISO_LOCAL_DATE);
                    eh = Integer.parseInt(hour2);
                    if (sh < 0 || sh > 23 || eh < 0 || eh > 23) throw new NumberFormatException();
                    if (ed.isBefore(sd) || (ed.equals(sd) && eh < sh)) throw new Exception();
                } catch (Exception ex) {
                    showError(resultPanel, "Invalid interval format or end time before start time.");
                    return;
                }
                java.util.Map<String, Integer> o3Map = new java.util.LinkedHashMap<>();
                java.util.Map<String, Integer> pm25Map = new java.util.LinkedHashMap<>();
                java.util.List<String> timeLabels = new java.util.ArrayList<>();
                LocalDateTime curr = LocalDateTime.of(sd, LocalTime.of(sh, 0));
                LocalDateTime end = LocalDateTime.of(ed, LocalTime.of(eh, 0));
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                while (!curr.isAfter(end)) {
                    timeLabels.add(curr.format(dtf));
                    curr = curr.plusHours(1);
                }
                for (String label : timeLabels) {
                    o3Map.put(label, null);
                    pm25Map.put(label, null);
                }
                try (BufferedReader br = new BufferedReader(new FileReader(HISTORICAL_DATA_FILE))) {
                    br.readLine();
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] arr = splitCSV(line);
                        if (arr.length < 7) continue;
                        if (!arr[0].equalsIgnoreCase(city)) continue;
                        String key = arr[2] + " " + arr[3];
                        if (!timeLabels.contains(key)) continue;
                        if (arr[4].equalsIgnoreCase("O3")) {
                            o3Map.put(key, parseIntSafe(arr[5]));
                        } else if (arr[4].equalsIgnoreCase("PM2.5")) {
                            pm25Map.put(key, parseIntSafe(arr[5]));
                        }
                    }
                } catch (Exception ex) {
                    showError(resultPanel, "Error reading historical file.");
                    return;
                }
                boolean hasData = false;
                for (String label : timeLabels) {
                    Integer o3val = o3Map.get(label);
                    Integer pm25val = pm25Map.get(label);
                    if ((o3val != null && o3val >= 0) || (pm25val != null && pm25val >= 0)) {
                        hasData = true;
                        break;
                    }
                }
                if (!hasData) {
                    showError(resultPanel, "No AQI data found for the requested interval.");
                } else {
                    AQIPlot plot = new AQIPlot(timeLabels, o3Map, pm25Map) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D)g;
                            int w = getWidth()-120, h = getHeight()-120, ox = 100, oy = 60;
                            g2.setColor(Color.WHITE);
                            g2.setStroke(new BasicStroke(3));
                            g2.drawLine(ox, oy, ox, oy+h);
                            g2.drawLine(ox, oy+h, ox+w, oy+h);

                            int maxAQI = 0;
                            for (String key : labels) {
                                Integer a = o3.get(key), b = pm25.get(key);
                                if (a != null && a > maxAQI) maxAQI = a;
                                if (b != null && b > maxAQI) maxAQI = b;
                            }
                            maxAQI = Math.max(maxAQI, 100);

                            g2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
                            for (int y=0; y<=maxAQI; y+=50) {
                                int py = oy+h - (int)(h * y / (double)maxAQI);
                                g2.drawLine(ox-7, py, ox, py);
                                g2.drawString(""+y, ox-50, py+7);
                            }

                            int n = labels.size();
                            if (n < 2) return;
                            int dx = w/(n-1);

                            g2.setStroke(new BasicStroke(3));
                            int prevX=-1, prevY=-1;
                            g2.setColor(new Color(0x2874A6));
                            for (int i=0; i<n; i++) {
                                Integer val = o3.get(labels.get(i));
                                if (val != null && val >= 0) {
                                    int x = ox + i*dx;
                                    int y = oy+h - (int)(h * val / (double)maxAQI);
                                    g2.fill(new Ellipse2D.Double(x-4, y-4, 8, 8));
                                    if (prevX != -1) g2.drawLine(prevX, prevY, x, y);
                                    prevX = x; prevY = y;
                                }
                            }
                            prevX = prevY = -1;
                            g2.setColor(new Color(0xF39C12));
                            for (int i=0; i<n; i++) {
                                Integer val = pm25.get(labels.get(i));
                                if (val != null && val >= 0) {
                                    int x = ox + i*dx;
                                    int y = oy+h - (int)(h * val / (double)maxAQI);
                                    g2.fill(new Ellipse2D.Double(x-4, y-4, 8, 8));
                                    if (prevX != -1) g2.drawLine(prevX, prevY, x, y);
                                    prevX = x; prevY = y;
                                }
                            }

                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font("Times New Roman", Font.PLAIN, 15));
                            int labelEvery = Math.max(1, n/10); // Fewer x-labels: only show at most 10 (adjust as needed)
                            int labelWidth = 80;
                            for (int i=0; i<n; i+=labelEvery) {
                                int x = ox + i*dx;
                                String lbl = labels.get(i);
                                // Show only date if many, or date+hour if few
                                String[] split = lbl.split(" ");
                                String xLabel = (n > 15 && split.length > 1) ? split[0] : lbl;
                                g2.drawString(xLabel, x-labelWidth/2, oy+h+25);
                            }

                            // Legend and axis labels unchanged...
                            g2.setFont(new Font("Times New Roman", Font.BOLD, 22));
                            g2.setColor(new Color(0x2874A6));
                            g2.fillRect(ox+80, oy-45, 34, 16);
                            g2.setColor(Color.WHITE);
                            g2.drawString("Ozone (O3)", ox+120, oy-32);
                            g2.setColor(new Color(0xF39C12));
                            g2.fillRect(ox+280, oy-45, 34, 16);
                            g2.setColor(Color.WHITE);
                            g2.drawString("PM2.5", ox+320, oy-32);

                            g2.setFont(new Font("Times New Roman", Font.PLAIN, 22));
                            g2.drawString("AQI", ox-60, oy-20);
                            g2.drawString("Time", ox + w/2 - 28, oy+h+55);
                        }
                    };
                    resultPanel.setLayout(new BorderLayout());
                    resultPanel.add(plot, BorderLayout.CENTER);
                }
            }
            resultPanel.revalidate();
            resultPanel.repaint();
        });

        mainPanel.add(historicalPanel, "historical");
    }
    
    private void showError(JPanel panel, String msg) {
        panel.removeAll();
        JLabel error = new JLabel("<html><div style='color:#FF4444; font-size:26pt;'><b>" + msg + "</b></div></html>", SwingConstants.CENTER);
        error.setFont(new Font("Times New Roman", Font.BOLD, 26));
        panel.add(error);
        panel.revalidate();
        panel.repaint();
    }

    private double[] getCoordinates(String city) {
        try {
            String urlCity = URLEncoder.encode(city, "UTF-8");
            String urlString = "https://nominatim.openstreetmap.org/search?city=" + urlCity
                    + "&format=json&limit=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestProperty("User-Agent", "AQIMonitor/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) sb.append(inputLine);
            in.close();
            conn.disconnect();

            JSONArray arr = new JSONArray(sb.toString());
            if (arr.length() == 0) return null;
            JSONObject obj = arr.getJSONObject(0);
            double lat = Double.parseDouble(obj.getString("lat"));
            double lon = Double.parseDouble(obj.getString("lon"));
            return new double[]{lat, lon};
        } catch (IOException e) {
            return null;
        }
    }

    private JSONArray fetchCurrentAQIFromAirNow(double lat, double lon) {
        try {
            String urlString = String.format(
                    "https://www.airnowapi.org/aq/observation/latLong/current/?format=application/json" +
                            "&latitude=%.4f&longitude=%.4f&distance=25&API_KEY=%s",
                    lat, lon, AIRNOW_API_KEY
            );
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestProperty("User-Agent", "AQIMonitor/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) sb.append(inputLine);
            in.close();
            conn.disconnect();

            return new JSONArray(sb.toString());
        } catch (IOException e) {
            return null;
        }
    }

    private java.util.List<String[]> getHistoricalRows(String city, LocalDate date, int hour) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORICAL_DATA_FILE))) {
            br.readLine();
            String line;
            String dateStr = date.toString();
            String hourStr = String.format("%02d", hour);
            while ((line = br.readLine()) != null) {
                String[] arr = splitCSV(line);
                if (arr.length < 7) continue;
                if (arr[0].equalsIgnoreCase(city) &&
                        arr[2].equals(dateStr) &&
                        arr[3].equals(hourStr)) {
                    result.add(arr);
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch(Exception e) { return -1; }
    }

    private static String[] splitCSV(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else sb.append(c);
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AQIMonitor2GUI gui = new AQIMonitor2GUI();
            gui.setVisible(true);
        });
    }

    static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int w = getWidth(), h = getHeight();
            Color c1 = new Color(23, 94, 181);
            Color c2 = new Color(70, 140, 220);
            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    static class AQIGaugeChart extends JPanel {
        int aqi;
        String category;
        String pollutant;

        public AQIGaugeChart(int aqi, String category, String pollutant) {
            this.aqi = aqi;
            this.category = category;
            this.pollutant = pollutant;
            setPreferredSize(new Dimension(540, 480));
            setOpaque(false);
        }

        private Color getCategoryColor(String category) {
            if (category == null) return Color.LIGHT_GRAY;
            switch (category.toLowerCase()) {
                case "good": return new Color(0x00E400);
                case "moderate": return new Color(0xFFFF00);
                case "unhealthy for sensitive groups": return new Color(0xFF7E00);
                case "unhealthy": return new Color(0xFF0000);
                case "very unhealthy": return new Color(0x8F3F97);
                case "hazardous": return new Color(0x7E0023);
                default: return Color.LIGHT_GRAY;
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int cx = getWidth()/2, cy = getHeight()/2 + 50, r = 180;
            int[] breaks = {0, 50, 100, 150, 200, 300, 500};
            Color[] colors = {
                new Color(0x00e400), new Color(0xffff00),
                new Color(0xff7e00), new Color(0xff0000),
                new Color(0x8f3f97), new Color(0x7e0023)
            };
            double start = 180, extent;
            for (int i=0; i<breaks.length-1; i++) {
                extent = -180.0 * (breaks[i+1] - breaks[i]) / 500;
                g2.setColor(colors[i]);
                Arc2D arc = new Arc2D.Double(cx - r, cy-r, 2*r, 2*r, start, extent, Arc2D.OPEN);
                g2.setStroke(new BasicStroke(28, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(arc);
                start += extent;
            }
            if (aqi >= 0) {
                double angle = Math.PI * (1 + (aqi / 500.0));
                int nx = (int)(cx + (r-40) * Math.cos(angle));
                int ny = (int)(cy + (r-40) * Math.sin(angle));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(8));
                g2.drawLine(cx, cy, nx, ny);
            }
            Color catColor = getCategoryColor(category);
            int ovalW = 260, ovalH = 120;
            g2.setColor(catColor);
            g2.fillOval(cx-ovalW/2, cy-r/2-ovalH/2, ovalW, ovalH);

            // Center the text vertically and horizontally in the oval
            String catText = category != null && !category.isEmpty() ? category : "N/A";
            g2.setColor(Color.BLACK);
            Font font = new Font("Times New Roman", Font.BOLD, 38);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(catText);
            int strH = fm.getHeight();
            int textX = cx - strW/2;
            int textY = cy-r/2-ovalH/2 + (ovalH + strH) / 2 - 8;
            g2.drawString(catText, textX, textY);
        }
    }

    static class AQIPlot extends JPanel {
        java.util.List<String> labels;
        java.util.Map<String, Integer> o3, pm25;

        AQIPlot(java.util.List<String> labels, java.util.Map<String, Integer> o3, java.util.Map<String, Integer> pm25) {
            this.labels = labels;
            this.o3 = o3;
            this.pm25 = pm25;
            setPreferredSize(new Dimension(950, 400));
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int w = getWidth()-120, h = getHeight()-120, ox = 100, oy = 60;
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(ox, oy, ox, oy+h);
            g2.drawLine(ox, oy+h, ox+w, oy+h);

            int maxAQI = 0;
            for (String key : labels) {
                Integer a = o3.get(key), b = pm25.get(key);
                if (a != null && a > maxAQI) maxAQI = a;
                if (b != null && b > maxAQI) maxAQI = b;
            }
            maxAQI = Math.max(maxAQI, 100);

            g2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
            for (int y=0; y<=maxAQI; y+=50) {
                int py = oy+h - (int)(h * y / (double)maxAQI);
                g2.drawLine(ox-7, py, ox, py);
                g2.drawString(""+y, ox-50, py+7);
            }

            int n = labels.size();
            if (n < 2) return;
            int dx = w/(n-1);

            g2.setStroke(new BasicStroke(3));
            int prevX=-1, prevY=-1;
            g2.setColor(new Color(0x2874A6));
            for (int i=0; i<n; i++) {
                Integer val = o3.get(labels.get(i));
                if (val != null && val >= 0) {
                    int x = ox + i*dx;
                    int y = oy+h - (int)(h * val / (double)maxAQI);
                    g2.fill(new Ellipse2D.Double(x-4, y-4, 8, 8));
                    if (prevX != -1) g2.drawLine(prevX, prevY, x, y);
                    prevX = x; prevY = y;
                }
            }
            prevX = prevY = -1;
            g2.setColor(new Color(0xF39C12));
            for (int i=0; i<n; i++) {
                Integer val = pm25.get(labels.get(i));
                if (val != null && val >= 0) {
                    int x = ox + i*dx;
                    int y = oy+h - (int)(h * val / (double)maxAQI);
                    g2.fill(new Ellipse2D.Double(x-4, y-4, 8, 8));
                    if (prevX != -1) g2.drawLine(prevX, prevY, x, y);
                    prevX = x; prevY = y;
                }
            }

            // --- X-axis labels (horizontal, MM-dd HH, no year, one per tick) ---
            g2.setColor(Color.WHITE);
            Font xLabelFont = new Font("Times New Roman", Font.PLAIN, 15);
            g2.setFont(xLabelFont);
            int maxLabels = Math.max(6, Math.min(12, w / 90));
            int labelEvery = Math.max(1, (int)Math.ceil((double)n / maxLabels));
            for (int i=0; i<n; i+=labelEvery) {
                int x = ox + i*dx;
                String lbl = labels.get(i);
                String shortLabel = lbl;
                // Strictly parse "yyyy-MM-dd HH" into "MM-dd HH"
                if (lbl.length() >= 13) {
                    try {
                        String mmdd = lbl.substring(5, 10);
                        String hour = lbl.length() >= 13 ? lbl.substring(11, 13) : "";
                        shortLabel = hour.isBlank() ? mmdd : (mmdd + " " + hour);
                    } catch (Exception e) {
                        shortLabel = lbl;
                    }
                }
                int labelWidth = g2.getFontMetrics().stringWidth(shortLabel);
                int lx = x - labelWidth/2;
                int ly = oy+h+25;
                g2.drawString(shortLabel, lx, ly);
            }
            // --- End x-labels ---

            // Legend and axis labels unchanged...
            g2.setFont(new Font("Times New Roman", Font.BOLD, 22));
            g2.setColor(new Color(0x2874A6));
            g2.fillRect(ox+80, oy-45, 34, 16);
            g2.setColor(Color.WHITE);
            g2.drawString("Ozone (O3)", ox+120, oy-32);
            g2.setColor(new Color(0xF39C12));
            g2.fillRect(ox+280, oy-45, 34, 16);
            g2.setColor(Color.WHITE);
            g2.drawString("PM2.5", ox+320, oy-32);

            g2.setFont(new Font("Times New Roman", Font.PLAIN, 22));
            g2.drawString("AQI", ox-60, oy-20);
            g2.drawString("Time", ox + w/2 - 28, oy+h+55);
        }
    }
}