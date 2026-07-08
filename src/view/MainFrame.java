package view;

import model.GymMember;
import model.Plan;
import model.RegularMember;
import controller.GymController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window composing all UI panels.
 */
public class MainFrame extends JFrame {

    private final MemberFormPanel formPanel;
    private final MemberTablePanel tablePanel;
    private GymController controller;

    public MainFrame(GymController controller) {
        this.controller = controller;

        setTitle("Gym Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 750);
        setLocationRelativeTo(null);

        setupLookAndFeel();

        // Create panels
        formPanel = new MemberFormPanel();
        tablePanel = new MemberTablePanel();

        // Setup layout
        setLayout(new BorderLayout());

        // Add Menu Bar (TOP)
        setJMenuBar(createMenuBar());

        // Add Form and Table (CENTER)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tablePanel);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.3);
        add(splitPane, BorderLayout.CENTER);

        // Setup table callback
        tablePanel.setControllerCallback(new MemberTablePanel.GymControllerCallback() {
            @Override
            public void onMemberSelected(GymMember memberOrNull) {
                // Not used anymore — kept for compatibility with MainFrame callback
            }

            @Override
            public void onFilterChanged() {
                if (controller != null) {
                    controller.refreshTable();
                }
            }
        });

        // Wire submit button
        wireSubmitButton();

        setVisible(true);
    }

    /**
     * Sets the Nimbus Look and Feel if available.
     */
    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus unavailable, using default look and feel: " + e.getMessage());
        }
    }

    /**
     * Creates the menu bar with File, Actions, Help menus and action buttons.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ==============================================
        // File Menu
        // ==============================================
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            try {
                controller.saveData();
            } catch (java.io.FileNotFoundException ex) {
                showInfo("No saved data found — starting with an empty member list.");
            } catch (java.io.IOException ex) {
                showError(ex.getMessage());
            }
        });

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            try {
                controller.loadData();
                controller.refreshTable();
                showInfo("Data loaded successfully.");
            } catch (java.io.FileNotFoundException ex) {
                showInfo("No saved data found — starting with an empty member list.");
            } catch (java.io.IOException ex) {
                showError(ex.getMessage());
            }
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(_ -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // ==============================================
        // Actions Menu
        // ==============================================
        JMenu actionsMenu = new JMenu("Actions");

        // Edit Selected
        JMenuItem editSelectedItem = new JMenuItem("Edit Selected");
        editSelectedItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to edit:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    GymMember member = controller.findMemberById(id);
                    if (member != null) {
                        formPanel.populateFromMember(member);
                    } else {
                        showError("Member not found: " + id);
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(editSelectedItem);
        actionsMenu.addSeparator();

        // Activate
        JMenuItem activateItem = new JMenuItem("Activate");
        activateItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to activate:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    controller.activateMember(id);
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(activateItem);

        // Deactivate
        JMenuItem deactivateItem = new JMenuItem("Deactivate");
        deactivateItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to deactivate:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    controller.deactivateMember(id);
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(deactivateItem);

        // Mark Attendance
        JMenuItem attendanceItem = new JMenuItem("Mark Attendance");
        attendanceItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID for attendance:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    controller.markAttendance(id);
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(attendanceItem);

        actionsMenu.addSeparator();

        // Upgrade Plan
        JMenuItem upgradeItem = new JMenuItem("Upgrade Plan");
        upgradeItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to upgrade:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    GymMember member = controller.findMemberById(id);
                    if (member == null) {
                        showError("Member not found: " + id);
                        return;
                    }
                    if (!(member instanceof RegularMember rm)) {
                        showError("Only Regular members can upgrade plans.");
                        return;
                    }
                    Plan selectedPlan = (Plan) JOptionPane.showInputDialog(this,
                            "Select new plan:", "Upgrade Plan", JOptionPane.QUESTION_MESSAGE,
                            null, Plan.values(), rm.getCurrentPlan());
                    if (selectedPlan != null) {
                        controller.upgradePlan(id, selectedPlan);
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(upgradeItem);

        // Pay Due
        JMenuItem payDueItem = new JMenuItem("Pay Due");
        payDueItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID for payment:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    String amountStr = JOptionPane.showInputDialog(this,
                            "Enter payment amount (Rs.):", "");
                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                        try {
                            long amountPaisa = MemberFormPanel.parseRupeesToPaisa(amountStr);
                            controller.payDueAmount(id, amountPaisa);
                        } catch (NumberFormatException ex) {
                            showError(ex.getMessage());
                        }
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(payDueItem);

        // Calculate Discount
        JMenuItem discountItem = new JMenuItem("Calculate Discount");
        discountItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Premium Member ID for discount:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    controller.calculateDiscount(id);
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(discountItem);

        actionsMenu.addSeparator();

        // Soft Delete
        JMenuItem softDeleteItem = new JMenuItem("Soft Delete");
        softDeleteItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to remove:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    String reason = JOptionPane.showInputDialog(this,
                            "Enter removal reason:", "");
                    if (reason != null && !reason.trim().isEmpty()) {
                        controller.softDeleteMember(id, reason);
                    } else if (reason != null) {
                        showError("Removal reason cannot be empty.");
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(softDeleteItem);

        // Restore
        JMenuItem restoreItem = new JMenuItem("Restore");
        restoreItem.addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            String idStr = JOptionPane.showInputDialog(this, "Enter Member ID to restore:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    controller.restoreMember(id);
                } catch (NumberFormatException ex) {
                    showError("Invalid ID format. Please enter a number.");
                }
            }
        });
        actionsMenu.add(restoreItem);

        menuBar.add(actionsMenu);

        // ==============================================
        // Help Menu
        // ==============================================
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(_ -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        // ==============================================
        // Separator (pushes remaining items to the right)
        // ==============================================
        menuBar.add(Box.createHorizontalGlue());


        // Utility Buttons on Menu Bar (Display All, Clear Form)
        JButton displayAllItem = new JButton("Display All");
        displayAllItem.setFocusPainted(false);
        displayAllItem.addActionListener(_ -> {
            if (controller != null) {
                controller.refreshTable();
            }
        });
        menuBar.add(displayAllItem);

        JButton clearFormItem = new JButton("Clear Form");
        clearFormItem.setFocusPainted(false);
        clearFormItem.addActionListener(_ -> formPanel.clearForm());
        menuBar.add(clearFormItem);
        menuBar.add(Box.createHorizontalStrut(10)); // Padding from the right screen edge

        return menuBar;
    }

    /**
     * Wires the submit button on the form.
     */
    private void wireSubmitButton() {
        formPanel.getSubmitButton().addActionListener(_ -> {
            if (controller == null) {
                showError("Controller not initialized yet.");
                return;
            }
            try {
                MemberFormData data = formPanel.collectFormData();
                if (formPanel.isEditMode()) {
                    if (data.id() != null) {
                        controller.updateMember(data.id(), data);
                    }
                } else {
                    controller.addMember(data);
                }
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    /**
     * Displays the About dialog with system information and usage guide.
     */
    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "About Gym Management System", true);
        aboutDialog.setSize(750, 650);
        aboutDialog.setLocationRelativeTo(this);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(getAboutContent());

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(_ -> aboutDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        aboutDialog.setLayout(new BorderLayout());
        aboutDialog.add(scrollPane, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);

        aboutDialog.setVisible(true);
    }

    /**
     * Returns the HTML content for the About dialog.
     */
    private String getAboutContent() {
        return """
        <html>
        <body style="font-family: Segoe UI, Arial, sans-serif; font-size: 13px; padding: 15px; width: 380px;">
        <div style="text-align: center; border-bottom: 2px solid #3498db; padding-bottom: 15px;">
            <h1 style="color: #2c3e50; font-size: 26px; margin: 0;">\uD83C\uDFCB\uFE0F Gym Management System</h1>
            <p style="color: #7f8c8d; font-size: 13px; margin: 5px 0 0 0;">Version 1.0</p>
            <p style="color: #95a5a6; font-size: 11px; margin: 2px 0 0 0;">Desktop Member Management Solution</p>
        </div>

        <div style="padding: 15px 0;">
            <h3 style="color: #2c3e50; margin: 0 0 5px 0;">Developer</h3>
            <p style="margin: 0 0 15px 0; color: #34495e;">Sabin</p>

            <h3 style="color: #2c3e50; margin: 0 0 5px 0;">Description</h3>
            <p style="margin: 0 0 15px 0; color: #34495e;">
                A complete gym member management solution for fitness centers, gyms, and health clubs.
                Built with a clean layered architecture to keep member data, business rules, and the
                user interface cleanly separated.
            </p>

            <h3 style="color: #2c3e50; margin: 0 0 5px 0;">Key Features</h3>
            <ul style="color: #34495e; margin: 0 0 15px 0; padding-left: 20px;">
                <li><b>Member Management:</b> Add, edit, and manage Regular and Premium members</li>
                <li><b>Attendance Tracking:</b> Mark daily attendance and earn loyalty points
                    (+5 Regular / +10 Premium per visit)</li>
                <li><b>Plan Upgrades:</b> Basic &rarr; Standard &rarr; Deluxe, unlocked after
                    30 visits with dues fully paid</li>
                <li><b>Payment Processing:</b> Record partial or full payments, track dues, and
                    apply a 10% discount for Premium members on full payment</li>
                <li><b>Soft Delete:</b> Remove members with a reason and restore them later</li>
                <li><b>Filter Views:</b> Switch between Active, Removed, and All members</li>
                <li><b>Data Persistence:</b> Save and load member data to and from CSV</li>
            </ul>

            <h3 style="color: #2c3e50; margin: 0 0 5px 0;">Built With</h3>
            <p style="margin: 0 0 15px 0; color: #34495e;">
                Java 25 &middot; Swing &middot; MVC Architecture &middot; CSV Persistence
            </p>
        </div>

        <hr style="border: 1px solid #ecf0f1;">
        <div style="text-align: center; padding: 10px 0 0 0;">
            <p style="color: #7f8c8d; font-size: 11px; margin: 0;">&copy; 2026 Sabin. All rights reserved.</p>
        </div>
        </body>
        </html>
        """;
    }

    // ========== Public Methods ==========

    public void setController(GymController controller) {
        this.controller = controller;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    public MemberFormPanel getFormPanel() {
        return formPanel;
    }

    public MemberTablePanel getTablePanel() {
        return tablePanel;
    }
}