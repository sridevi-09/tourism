import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourismManagementSystem extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> panels = new HashMap<>();

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tourism_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "dbms";
    private Connection connection;

    public TourismManagementSystem() {
        configureFrame();
        initializeDatabase();
        createPanels();
        showLoginScreen();
    }

    private void configureFrame() {
        setTitle("Tourism Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(mainPanel);
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create destinations table
            stmt.execute("CREATE TABLE IF NOT EXISTS destinations (" +
                    "id VARCHAR(10) PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "country VARCHAR(50) NOT NULL, " +
                    "description TEXT, " +
                    "best_season VARCHAR(20))");

            // Create tours table
            stmt.execute("CREATE TABLE IF NOT EXISTS tours (" +
                    "id VARCHAR(10) PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "destination_id VARCHAR(10) NOT NULL, " +
                    "duration_days INT NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "available BOOLEAN DEFAULT TRUE, " +
                    "FOREIGN KEY (destination_id) REFERENCES destinations(id))");

            // Create customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id VARCHAR(10) PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(50) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "address TEXT)");

            // Create bookings table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id VARCHAR(10) PRIMARY KEY, " +
                    "tour_id VARCHAR(10) NOT NULL, " +
                    "customer_id VARCHAR(10) NOT NULL, " +
                    "booking_date DATE NOT NULL, " +
                    "travel_date DATE NOT NULL, " +
                    "num_people INT NOT NULL, " +
                    "total_price DECIMAL(10,2) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'Confirmed', " +
                    "FOREIGN KEY (tour_id) REFERENCES tours(id), " +
                    "FOREIGN KEY (customer_id) REFERENCES customers(id))");
        }
    }

    private void createPanels() {
        panels.put("login", createLoginPanel());
        panels.put("dashboard", createDashboardPanel());
        panels.put("destination", createDestinationPanel());
        panels.put("tour", createTourPanel());
        panels.put("customer", createCustomerPanel());
        panels.put("booking", createBookingPanel());

        for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
            mainPanel.add(entry.getValue(), entry.getKey());
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new GradientPanel(new Color(44, 62, 80), new Color(52, 73, 94));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel logo = new JLabel("Tourism Management System", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        panel.add(logo, gbc);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);

        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");

        form.add(new JLabel("Username:"));
        form.add(username);
        form.add(new JLabel("Password:"));
        form.add(password);
        form.add(new JLabel());
        form.add(loginBtn);

        loginBtn.addActionListener(e -> {
            if ("admin".equals(username.getText()) && "admin123".equals(new String(password.getPassword()))) {
                cardLayout.show(mainPanel, "dashboard");
            } else {
                JOptionPane.showMessageDialog(panel, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(form, gbc);
        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new GradientPanel(new Color(52, 152, 219), new Color(41, 128, 185));
        panel.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Tourism Management Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        header.add(logout, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 2, 20, 20));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] modules = {"Destination", "Tour", "Customer", "Booking"};
        String[] icons = {"🌍", "✈️", "👤", "📅"};

        for (int i = 0; i < modules.length; i++) {
            JPanel card = createCard(modules[i], icons[i]);
            final String panelName = modules[i].toLowerCase();
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, panelName);
                    refreshTable(panelName);
                }
            });
            cards.add(card);
        }

        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCard(String title, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255, 150));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(44, 62, 80));

        card.add(iconLbl, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDestinationPanel() {
        JPanel panel = new GradientPanel(new Color(46, 204, 113), new Color(39, 174, 96));
        panel.setLayout(new BorderLayout());

        panel.add(createModuleHeader("Destination Management"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);

        JButton addBtn = new JButton("Add Destination");
        addBtn.addActionListener(e -> showDestinationForm(null));
        toolbar.add(addBtn);

        JButton editBtn = new JButton("Edit Destination");
        editBtn.addActionListener(e -> editSelectedDestination());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("Delete Destination");
        deleteBtn.addActionListener(e -> deleteSelectedDestination());
        toolbar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable("destination"));
        toolbar.add(refreshBtn);

        content.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Country", "Best Season"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedDestination();
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        panel.add(createFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private void editSelectedDestination() {
        JTable table = getTableFromPanel(panels.get("destination"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String destinationId = (String) table.getValueAt(row, 0);
                try {
                    Destination destination = Destination.getById(connection, destinationId);
                    if (destination != null) {
                        showDestinationForm(destination);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading destination data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteSelectedDestination() {
        JTable table = getTableFromPanel(panels.get("destination"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String destinationId = (String) table.getValueAt(row, 0);
                String destinationName = (String) table.getValueAt(row, 1);

                try {
                    // Check if destination is referenced in tours
                    if (isDestinationReferenced(destinationId)) {
                        JOptionPane.showMessageDialog(this,
                                "Cannot delete destination. It is referenced in tour records.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete destination " + destinationName + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (Destination.delete(connection, destinationId)) {
                            refreshTable("destination");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete destination", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private boolean isDestinationReferenced(String destinationId) throws SQLException {
        String query = "SELECT COUNT(*) FROM tours WHERE destination_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, destinationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        }
        return false;
    }

    private JPanel createTourPanel() {
        JPanel panel = new GradientPanel(new Color(155, 89, 182), new Color(142, 68, 173));
        panel.setLayout(new BorderLayout());

        panel.add(createModuleHeader("Tour Management"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);

        JButton addBtn = new JButton("Add Tour");
        addBtn.addActionListener(e -> showTourForm(null));
        toolbar.add(addBtn);

        JButton editBtn = new JButton("Edit Tour");
        editBtn.addActionListener(e -> editSelectedTour());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("Delete Tour");
        deleteBtn.addActionListener(e -> deleteSelectedTour());
        toolbar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable("tour"));
        toolbar.add(refreshBtn);

        content.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Destination", "Duration (Days)", "Price", "Available"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTour();
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        panel.add(createFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private void editSelectedTour() {
        JTable table = getTableFromPanel(panels.get("tour"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String tourId = (String) table.getValueAt(row, 0);
                try {
                    Tour tour = Tour.getById(connection, tourId);
                    if (tour != null) {
                        showTourForm(tour);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading tour data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tour to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteSelectedTour() {
        JTable table = getTableFromPanel(panels.get("tour"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String tourId = (String) table.getValueAt(row, 0);
                String tourName = (String) table.getValueAt(row, 1);

                try {
                    // Check if tour is referenced in bookings
                    if (isTourReferenced(tourId)) {
                        JOptionPane.showMessageDialog(this,
                                "Cannot delete tour. It is referenced in booking records.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete tour " + tourName + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (Tour.delete(connection, tourId)) {
                            refreshTable("tour");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete tour", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tour to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private boolean isTourReferenced(String tourId) throws SQLException {
        String query = "SELECT COUNT(*) FROM bookings WHERE tour_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tourId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        }
        return false;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new GradientPanel(new Color(52, 152, 219), new Color(41, 128, 185));
        panel.setLayout(new BorderLayout());

        panel.add(createModuleHeader("Customer Management"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);

        JButton addBtn = new JButton("Add Customer");
        addBtn.addActionListener(e -> showCustomerForm(null));
        toolbar.add(addBtn);

        JButton editBtn = new JButton("Edit Customer");
        editBtn.addActionListener(e -> editSelectedCustomer());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("Delete Customer");
        deleteBtn.addActionListener(e -> deleteSelectedCustomer());
        toolbar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable("customer"));
        toolbar.add(refreshBtn);

        content.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCustomer();
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        panel.add(createFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private void editSelectedCustomer() {
        JTable table = getTableFromPanel(panels.get("customer"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String customerId = (String) table.getValueAt(row, 0);
                try {
                    Customer customer = Customer.getById(connection, customerId);
                    if (customer != null) {
                        showCustomerForm(customer);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading customer data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a customer to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteSelectedCustomer() {
        JTable table = getTableFromPanel(panels.get("customer"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String customerId = (String) table.getValueAt(row, 0);
                String customerName = (String) table.getValueAt(row, 1);

                try {
                    // Check if customer is referenced in bookings
                    if (isCustomerReferenced(customerId)) {
                        JOptionPane.showMessageDialog(this,
                                "Cannot delete customer. It is referenced in booking records.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete customer " + customerName + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (Customer.delete(connection, customerId)) {
                            refreshTable("customer");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete customer", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a customer to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private boolean isCustomerReferenced(String customerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM bookings WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return true;
        }
        return false;
    }

    private JPanel createBookingPanel() {
        JPanel panel = new GradientPanel(new Color(230, 126, 34), new Color(211, 84, 0));
        panel.setLayout(new BorderLayout());

        panel.add(createModuleHeader("Booking Management"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);

        JButton addBtn = new JButton("Add Booking");
        addBtn.addActionListener(e -> showBookingForm(null));
        toolbar.add(addBtn);

        JButton editBtn = new JButton("Edit Booking");
        editBtn.addActionListener(e -> editSelectedBooking());
        toolbar.add(editBtn);

        JButton deleteBtn = new JButton("Delete Booking");
        deleteBtn.addActionListener(e -> deleteSelectedBooking());
        toolbar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable("booking"));
        toolbar.add(refreshBtn);

        content.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "Tour", "Customer", "Booking Date", "Travel Date", "People", "Total Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedBooking();
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);

        panel.add(createFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private void editSelectedBooking() {
        JTable table = getTableFromPanel(panels.get("booking"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String bookingId = (String) table.getValueAt(row, 0);
                try {
                    Booking booking = Booking.getById(connection, bookingId);
                    if (booking != null) {
                        showBookingForm(booking);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading booking data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a booking to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteSelectedBooking() {
        JTable table = getTableFromPanel(panels.get("booking"));
        if (table != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String bookingId = (String) table.getValueAt(row, 0);
                String tourName = (String) table.getValueAt(row, 1);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete booking for " + tourName + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (Booking.delete(connection, bookingId)) {
                            refreshTable("booking");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete booking", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a booking to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JPanel createModuleHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        header.add(backBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel status = new JLabel("Tourism Management System v1.0");
        status.setForeground(Color.WHITE);
        footer.add(status, BorderLayout.WEST);

        JButton settings = new JButton("Settings");
        settings.addActionListener(e -> showSettings());
        footer.add(settings, BorderLayout.EAST);

        return footer;
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings would be implemented here", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoginScreen() {
        cardLayout.show(mainPanel, "login");
    }

    private JTable getTableFromPanel(JPanel panel) {
        if (panel == null) return null;

        for (Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                return (JTable) scrollPane.getViewport().getView();
            }
            if (comp instanceof JPanel) {
                JTable table = getTableFromPanel((JPanel) comp);
                if (table != null) return table;
            }
        }
        return null;
    }

    private void showDestinationForm(Destination destination) {
        JDialog dialog = new JDialog(this, destination == null ? "Add Destination" : "Edit Destination", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField countryField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JComboBox<String> seasonCombo = new JComboBox<>(new String[]{"Spring", "Summer", "Autumn", "Winter", "All Year"});

        if (destination != null) {
            idField.setText(destination.getId());
            nameField.setText(destination.getName());
            countryField.setText(destination.getCountry());
            descriptionArea.setText(destination.getDescription());
            seasonCombo.setSelectedItem(destination.getBestSeason());
            idField.setEditable(false);
        }

        form.add(new JLabel("Destination ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Country:"));
        form.add(countryField);
        form.add(new JLabel("Description:"));
        form.add(descriptionScroll);
        form.add(new JLabel("Best Season:"));
        form.add(seasonCombo);

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String country = countryField.getText();
            String description = descriptionArea.getText();
            String bestSeason = (String) seasonCombo.getSelectedItem();

            if (id.isEmpty() || name.isEmpty() || country.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Destination d = new Destination(id, name, country, description, bestSeason);
                if (d.save(connection)) {
                    refreshTable("destination");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save destination", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showTourForm(Tour tour) {
        JDialog dialog = new JDialog(this, tour == null ? "Add Tour" : "Edit Tour", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> destinationCombo = new JComboBox<>();
        JTextField durationField = new JTextField();
        JTextField priceField = new JTextField();
        JCheckBox availableCheck = new JCheckBox("Available");

        try {
            List<Destination> destinations = Destination.getAllDestinations(connection);
            for (Destination d : destinations) {
                destinationCombo.addItem(d.getName() + " (" + d.getId() + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (tour != null) {
            idField.setText(tour.getId());
            nameField.setText(tour.getName());
            try {
                Destination destination = Destination.getById(connection, tour.getDestinationId());
                if (destination != null) {
                    destinationCombo.setSelectedItem(destination.getName() + " (" + destination.getId() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            durationField.setText(String.valueOf(tour.getDurationDays()));
            priceField.setText(String.valueOf(tour.getPrice()));
            availableCheck.setSelected(tour.isAvailable());
            idField.setEditable(false);
        }

        form.add(new JLabel("Tour ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Destination:"));
        form.add(destinationCombo);
        form.add(new JLabel("Duration (Days):"));
        form.add(durationField);
        form.add(new JLabel("Price:"));
        form.add(priceField);
        form.add(new JLabel("Availability:"));
        form.add(availableCheck);

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String destinationSelection = (String) destinationCombo.getSelectedItem();
            String durationStr = durationField.getText();
            String priceStr = priceField.getText();
            boolean available = availableCheck.isSelected();

            if (id.isEmpty() || name.isEmpty() || destinationSelection == null ||
                    durationStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Extract destination ID from selection
                String destinationId = destinationSelection.substring(destinationSelection.indexOf("(") + 1, destinationSelection.indexOf(")"));

                int duration = Integer.parseInt(durationStr);
                double price = Double.parseDouble(priceStr);

                Tour t = new Tour(id, name, destinationId, duration, price, available);
                if (t.save(connection)) {
                    refreshTable("tour");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save tour", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duration and price must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCustomerForm(Customer customer) {
        JDialog dialog = new JDialog(this, customer == null ? "Add Customer" : "Edit Customer", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextArea addressArea = new JTextArea(3, 20);
        JScrollPane addressScroll = new JScrollPane(addressArea);

        if (customer != null) {
            idField.setText(customer.getId());
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
            addressArea.setText(customer.getAddress());
            idField.setEditable(false);
        }

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Address:"));
        form.add(addressScroll);

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressArea.getText();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Customer c = new Customer(id, name, email, phone, address);
                if (c.save(connection)) {
                    refreshTable("customer");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save customer", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showBookingForm(Booking booking) {
        JDialog dialog = new JDialog(this, booking == null ? "Add Booking" : "Edit Booking", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));

        JTextField idField = new JTextField();
        JComboBox<String> tourCombo = new JComboBox<>();
        JComboBox<String> customerCombo = new JComboBox<>();
        JTextField bookingDateField = new JTextField();
        JTextField travelDateField = new JTextField();
        JTextField numPeopleField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Confirmed", "Pending", "Cancelled", "Completed"});

        try {
            List<Tour> tours = Tour.getAllTours(connection);
            for (Tour t : tours) {
                tourCombo.addItem(t.getName() + " (" + t.getId() + ")");
            }

            List<Customer> customers = Customer.getAllCustomers(connection);
            for (Customer c : customers) {
                customerCombo.addItem(c.getName() + " (" + c.getId() + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (booking != null) {
            idField.setText(booking.getId());
            try {
                Tour tour = Tour.getById(connection, booking.getTourId());
                if (tour != null) {
                    tourCombo.setSelectedItem(tour.getName() + " (" + tour.getId() + ")");
                }

                Customer customer = Customer.getById(connection, booking.getCustomerId());
                if (customer != null) {
                    customerCombo.setSelectedItem(customer.getName() + " (" + customer.getId() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            bookingDateField.setText(booking.getBookingDate().toString());
            travelDateField.setText(booking.getTravelDate().toString());
            numPeopleField.setText(String.valueOf(booking.getNumPeople()));
            statusCombo.setSelectedItem(booking.getStatus());
            idField.setEditable(false);
        }

        form.add(new JLabel("Booking ID:"));
        form.add(idField);
        form.add(new JLabel("Tour:"));
        form.add(tourCombo);
        form.add(new JLabel("Customer:"));
        form.add(customerCombo);
        form.add(new JLabel("Booking Date (YYYY-MM-DD):"));
        form.add(bookingDateField);
        form.add(new JLabel("Travel Date (YYYY-MM-DD):"));
        form.add(travelDateField);
        form.add(new JLabel("Number of People:"));
        form.add(numPeopleField);
        form.add(new JLabel("Status:"));
        form.add(statusCombo);

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> {
            String id = idField.getText();
            String tourSelection = (String) tourCombo.getSelectedItem();
            String customerSelection = (String) customerCombo.getSelectedItem();
            String bookingDate = bookingDateField.getText();
            String travelDate = travelDateField.getText();
            String numPeopleStr = numPeopleField.getText();
            String status = (String) statusCombo.getSelectedItem();

            if (id.isEmpty() || tourSelection == null || customerSelection == null ||
                    bookingDate.isEmpty() || travelDate.isEmpty() || numPeopleStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Extract tour and customer IDs from selections
                String tourId = tourSelection.substring(tourSelection.indexOf("(") + 1, tourSelection.indexOf(")"));
                String customerId = customerSelection.substring(customerSelection.indexOf("(") + 1, customerSelection.indexOf(")"));

                int numPeople = Integer.parseInt(numPeopleStr);

                // Get tour price
                Tour tour = Tour.getById(connection, tourId);
                double totalPrice = numPeople * tour.getPrice();

                Booking b = new Booking(id, tourId, customerId, bookingDate, travelDate, numPeople, totalPrice, status);
                if (b.save(connection)) {
                    refreshTable("booking");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save booking", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Number of people must be an integer", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void refreshTable(String panelName) {
        try {
            JPanel panel = panels.get(panelName);
            JTable table = getTableFromPanel(panel);
            if (table != null) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);

                switch (panelName) {
                    case "destination":
                        List<Destination> destinations = Destination.getAllDestinations(connection);
                        for (Destination d : destinations) {
                            model.addRow(new Object[]{d.getId(), d.getName(), d.getCountry(), d.getBestSeason()});
                        }
                        break;
                    case "tour":
                        List<Tour> tours = Tour.getAllTours(connection);
                        for (Tour t : tours) {
                            model.addRow(new Object[]{
                                    t.getId(),
                                    t.getName(),
                                    t.getDestinationName(connection),
                                    t.getDurationDays(),
                                    t.getPrice(),
                                    t.isAvailable() ? "Yes" : "No"
                            });
                        }
                        break;
                    case "customer":
                        List<Customer> customers = Customer.getAllCustomers(connection);
                        for (Customer c : customers) {
                            model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone()});
                        }
                        break;
                    case "booking":
                        List<Booking> bookings = Booking.getAllBookings(connection);
                        for (Booking b : bookings) {
                            model.addRow(new Object[]{
                                    b.getId(),
                                    b.getTourName(connection),
                                    b.getCustomerName(connection),
                                    b.getBookingDate(),
                                    b.getTravelDate(),
                                    b.getNumPeople(),
                                    b.getTotalPrice(),
                                    b.getStatus()
                            });
                        }
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                TourismManagementSystem frame = new TourismManagementSystem();
                frame.setVisible(true);

                // Add window listener to close database connection
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            if (frame.connection != null && !frame.connection.isClosed()) {
                                frame.connection.close();
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

class GradientPanel extends JPanel {
    private Color color1;
    private Color color2;

    public GradientPanel(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

class Destination {
    private String id;
    private String name;
    private String country;
    private String description;
    private String bestSeason;

    public Destination(String id, String name, String country, String description, String bestSeason) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.description = description;
        this.bestSeason = bestSeason;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getDescription() { return description; }
    public String getBestSeason() { return bestSeason; }

    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
    public void setDescription(String description) { this.description = description; }
    public void setBestSeason(String bestSeason) { this.bestSeason = bestSeason; }

    // Database operations
    public static List<Destination> getAllDestinations(Connection conn) throws SQLException {
        List<Destination> destinations = new ArrayList<>();
        String query = "SELECT * FROM destinations";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                destinations.add(new Destination(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("description"),
                        rs.getString("best_season")
                ));
            }
        }
        return destinations;
    }

    public static Destination getById(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM destinations WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Destination(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("description"),
                        rs.getString("best_season")
                );
            }
        }
        return null;
    }

    public boolean save(Connection conn) throws SQLException {
        if (getById(conn, this.id) != null) {
            return update(conn);
        } else {
            return insert(conn);
        }
    }

    private boolean insert(Connection conn) throws SQLException {
        String query = "INSERT INTO destinations (id, name, country, description, best_season) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, country);
            pstmt.setString(4, description);
            pstmt.setString(5, bestSeason);

            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean update(Connection conn) throws SQLException {
        String query = "UPDATE destinations SET name = ?, country = ?, description = ?, best_season = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, country);
            pstmt.setString(3, description);
            pstmt.setString(4, bestSeason);
            pstmt.setString(5, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection conn, String id) throws SQLException {
        String query = "DELETE FROM destinations WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}

class Tour {
    private String id;
    private String name;
    private String destinationId;
    private int durationDays;
    private double price;
    private boolean available;

    public Tour(String id, String name, String destinationId, int durationDays, double price, boolean available) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.durationDays = durationDays;
        this.price = price;
        this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDestinationId() { return destinationId; }
    public int getDurationDays() { return durationDays; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setName(String name) { this.name = name; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    // Database operations
    public static List<Tour> getAllTours(Connection conn) throws SQLException {
        List<Tour> tours = new ArrayList<>();
        String query = "SELECT * FROM tours";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tours.add(new Tour(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("destination_id"),
                        rs.getInt("duration_days"),
                        rs.getDouble("price"),
                        rs.getBoolean("available")
                ));
            }
        }
        return tours;
    }

    public static Tour getById(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM tours WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Tour(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("destination_id"),
                        rs.getInt("duration_days"),
                        rs.getDouble("price"),
                        rs.getBoolean("available")
                );
            }
        }
        return null;
    }

    public String getDestinationName(Connection conn) throws SQLException {
        Destination destination = Destination.getById(conn, destinationId);
        return destination != null ? destination.getName() : "Unknown Destination";
    }

    public boolean save(Connection conn) throws SQLException {
        if (getById(conn, this.id) != null) {
            return update(conn);
        } else {
            return insert(conn);
        }
    }

    private boolean insert(Connection conn) throws SQLException {
        String query = "INSERT INTO tours (id, name, destination_id, duration_days, price, available) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, destinationId);
            pstmt.setInt(4, durationDays);
            pstmt.setDouble(5, price);
            pstmt.setBoolean(6, available);

            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean update(Connection conn) throws SQLException {
        String query = "UPDATE tours SET name = ?, destination_id = ?, duration_days = ?, " +
                "price = ?, available = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, destinationId);
            pstmt.setInt(3, durationDays);
            pstmt.setDouble(4, price);
            pstmt.setBoolean(5, available);
            pstmt.setString(6, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection conn, String id) throws SQLException {
        String query = "DELETE FROM tours WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}

class Customer {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;

    public Customer(String id, String name, String email, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }

    // Database operations
    public static List<Customer> getAllCustomers(Connection conn) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        }
        return customers;
    }

    public static Customer getById(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM customers WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                );
            }
        }
        return null;
    }

    public boolean save(Connection conn) throws SQLException {
        if (getById(conn, this.id) != null) {
            return update(conn);
        } else {
            return insert(conn);
        }
    }

    private boolean insert(Connection conn) throws SQLException {
        String query = "INSERT INTO customers (id, name, email, phone, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);

            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean update(Connection conn) throws SQLException {
        String query = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setString(5, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection conn, String id) throws SQLException {
        String query = "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}

class Booking {
    private String id;
    private String tourId;
    private String customerId;
    private Date bookingDate;
    private Date travelDate;
    private int numPeople;
    private double totalPrice;
    private String status;

    public Booking(String id, String tourId, String customerId, String bookingDate,
                   String travelDate, int numPeople, double totalPrice, String status) {
        this.id = id;
        this.tourId = tourId;
        this.customerId = customerId;
        this.bookingDate = Date.valueOf(bookingDate);
        this.travelDate = Date.valueOf(travelDate);
        this.numPeople = numPeople;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTourId() { return tourId; }
    public String getCustomerId() { return customerId; }
    public Date getBookingDate() { return bookingDate; }
    public Date getTravelDate() { return travelDate; }
    public int getNumPeople() { return numPeople; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    public void setTourId(String tourId) { this.tourId = tourId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    public void setTravelDate(Date travelDate) { this.travelDate = travelDate; }
    public void setNumPeople(int numPeople) { this.numPeople = numPeople; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }

    // Database operations
    public static List<Booking> getAllBookings(Connection conn) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM bookings";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getString("id"),
                        rs.getString("tour_id"),
                        rs.getString("customer_id"),
                        rs.getDate("booking_date").toString(),
                        rs.getDate("travel_date").toString(),
                        rs.getInt("num_people"),
                        rs.getDouble("total_price"),
                        rs.getString("status")
                ));
            }
        }
        return bookings;
    }

    public static Booking getById(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM bookings WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Booking(
                        rs.getString("id"),
                        rs.getString("tour_id"),
                        rs.getString("customer_id"),
                        rs.getDate("booking_date").toString(),
                        rs.getDate("travel_date").toString(),
                        rs.getInt("num_people"),
                        rs.getDouble("total_price"),
                        rs.getString("status")
                );
            }
        }
        return null;
    }

    public String getTourName(Connection conn) throws SQLException {
        Tour tour = Tour.getById(conn, tourId);
        return tour != null ? tour.getName() : "Unknown Tour";
    }

    public String getCustomerName(Connection conn) throws SQLException {
        Customer customer = Customer.getById(conn, customerId);
        return customer != null ? customer.getName() : "Unknown Customer";
    }

    public boolean save(Connection conn) throws SQLException {
        if (getById(conn, this.id) != null) {
            return update(conn);
        } else {
            return insert(conn);
        }
    }

    private boolean insert(Connection conn) throws SQLException {
        String query = "INSERT INTO bookings (id, tour_id, customer_id, booking_date, " +
                "travel_date, num_people, total_price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.setString(2, tourId);
            pstmt.setString(3, customerId);
            pstmt.setDate(4, bookingDate);
            pstmt.setDate(5, travelDate);
            pstmt.setInt(6, numPeople);
            pstmt.setDouble(7, totalPrice);
            pstmt.setString(8, status);

            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean update(Connection conn) throws SQLException {
        String query = "UPDATE bookings SET tour_id = ?, customer_id = ?, booking_date = ?, " +
                "travel_date = ?, num_people = ?, total_price = ?, status = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tourId);
            pstmt.setString(2, customerId);
            pstmt.setDate(3, bookingDate);
            pstmt.setDate(4, travelDate);
            pstmt.setInt(5, numPeople);
            pstmt.setDouble(6, totalPrice);
            pstmt.setString(7, status);
            pstmt.setString(8, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(Connection conn, String id) throws SQLException {
        String query = "DELETE FROM bookings WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}