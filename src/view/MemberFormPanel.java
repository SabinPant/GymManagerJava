package view;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Input form for adding new members and editing contact/identity fields.
 */
public class MemberFormPanel extends JPanel {

    // Personal Information fields
    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(20);
    private final JTextField locationField = new JTextField(20);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(20);

    // Gender radio buttons
    private final JRadioButton maleRadio = new JRadioButton("Male", true);
    private final JRadioButton femaleRadio = new JRadioButton("Female");
    private final JRadioButton otherRadio = new JRadioButton("Other");
    private final JRadioButton prefersRadio = new JRadioButton("Prefer not to say");

    // Date of birth combos
    private final JComboBox<Integer> dobDayCombo;
    private final JComboBox<String> dobMonthCombo;
    private final JComboBox<Integer> dobYearCombo;

    // Membership start date combos
    private final JComboBox<Integer> startDayCombo;
    private final JComboBox<String> startMonthCombo;
    private final JComboBox<Integer> startYearCombo;

    // Member type radio buttons
    private final JRadioButton regularRadio = new JRadioButton("Regular", true);
    private final JRadioButton premiumRadio = new JRadioButton("Premium");

    // Member type-specific fields
    private final JTextField referralField = new JTextField(20);
    private final JTextField trainerField = new JTextField(20);
    private final JComboBox<model.Plan> planCombo;

    // Payment info
    private final JTextField paidAmountField = new JTextField(15);

    // Submit button
    private final JButton submitButton = new JButton("Add Member");

    private boolean isEditMode = false;

    public MemberFormPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Compute year range dynamically
        int currentYear = java.time.Year.now().getValue();
        Integer[] years = java.util.stream.IntStream.rangeClosed(currentYear - 100, currentYear)
                .boxed()
                .sorted(Collections.reverseOrder())
                .toArray(Integer[]::new);

        // Initialize date combos
        dobDayCombo = new JComboBox<>(createDayArray());
        dobMonthCombo = new JComboBox<>(createMonthArray());
        dobYearCombo = new JComboBox<>(years);

        startDayCombo = new JComboBox<>(createDayArray());
        startMonthCombo = new JComboBox<>(createMonthArray());
        startYearCombo = new JComboBox<>(years);

        planCombo = new JComboBox<>(model.Plan.values());

        // Section 1: Personal Information
        JPanel section1 = createPersonalInfoSection();
        JScrollPane scroll1 = new JScrollPane(section1);
        scroll1.setBorder(BorderFactory.createTitledBorder("Personal Information"));
        add(scroll1);

        // Section 2: Membership Information
        JPanel section2 = createMembershipInfoSection();
        JScrollPane scroll2 = new JScrollPane(section2);
        scroll2.setBorder(BorderFactory.createTitledBorder("Membership Information"));
        add(scroll2);

        // Section 3: Payment Information
        JPanel section3 = createPaymentInfoSection();
        JScrollPane scroll3 = new JScrollPane(section3);
        scroll3.setBorder(BorderFactory.createTitledBorder("Payment Information"));
        add(scroll3);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        add(buttonPanel);

        // Setup member type toggle
        setupMemberTypeToggle();
    }

    private JPanel createPersonalInfoSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        panel.add(locationField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Gender:"), gbc);
        JPanel genderPanel = new JPanel();
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderGroup.add(otherRadio);
        genderGroup.add(prefersRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        genderPanel.add(otherRadio);
        genderPanel.add(prefersRadio);
        gbc.gridx = 1;
        panel.add(genderPanel, gbc);

        return panel;
    }

    private JPanel createMembershipInfoSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Date of Birth:"), gbc);
        JPanel dobPanel = new JPanel();
        dobPanel.add(dobDayCombo);
        dobPanel.add(dobMonthCombo);
        dobPanel.add(dobYearCombo);
        gbc.gridx = 1;
        panel.add(dobPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Start Date:"), gbc);
        JPanel startPanel = new JPanel();
        startPanel.add(startDayCombo);
        startPanel.add(startMonthCombo);
        startPanel.add(startYearCombo);
        gbc.gridx = 1;
        panel.add(startPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Member Type:"), gbc);
        JPanel typePanel = new JPanel();
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(regularRadio);
        typeGroup.add(premiumRadio);
        typePanel.add(regularRadio);
        typePanel.add(premiumRadio);
        gbc.gridx = 1;
        panel.add(typePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Referral Source:"), gbc);
        gbc.gridx = 1;
        panel.add(referralField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Personal Trainer:"), gbc);
        trainerField.setVisible(false);
        gbc.gridx = 1;
        panel.add(trainerField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Plan:"), gbc);
        gbc.gridx = 1;
        panel.add(planCombo, gbc);

        return panel;
    }

    private JPanel createPaymentInfoSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Paid Amount (Rs.):"), gbc);
        gbc.gridx = 1;
        panel.add(paidAmountField, gbc);

        return panel;
    }

    private void setupMemberTypeToggle() {
        regularRadio.addActionListener(_ -> {
            trainerField.setText("");
            trainerField.setVisible(false);
            referralField.setVisible(true);
            planCombo.setEnabled(true);
            revalidate();
            repaint();
        });
        premiumRadio.addActionListener(_ -> {
            referralField.setText("");
            referralField.setVisible(false);
            trainerField.setVisible(true);
            planCombo.setEnabled(false);
            revalidate();
            repaint();
        });
    }

    /**
     * Suggests the next available member ID: (max existing ID) + 1, or 1 if no members exist.
     */
    public static int suggestNextId(List<model.GymMember> allMembers) {
        return allMembers.stream()
                .mapToInt(model.GymMember::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Parses a rupee amount string into paisa using integer arithmetic only.
     */
    public static long parseRupeesToPaisa(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }
        String trimmed = input.trim();

        if (!trimmed.matches("\\d+(\\.\\d{1,2})?")) {
            throw new NumberFormatException(
                    "Enter a valid amount (e.g., 6500 or 6500.50). Max 2 decimal places."
            );
        }

        String[] parts = trimmed.split("\\.");
        long rupees = Long.parseLong(parts[0]);
        long paisaPart = 0;
        if (parts.length == 2) {
            String decimals = parts[1];
            if (decimals.length() == 1) decimals = decimals + "0";
            paisaPart = Long.parseLong(decimals);
        }

        long totalPaisa = rupees * 100 + paisaPart;
        if (totalPaisa < 0) {
            throw new NumberFormatException("Amount cannot be negative");
        }
        return totalPaisa;
    }

    /**
     * Gathers form field values into a MemberFormData record.
     */
    public MemberFormData collectFormData() {
        String dobStr = formatDate(
                (Integer) dobDayCombo.getSelectedItem(),
                (String) dobMonthCombo.getSelectedItem(),
                (Integer) dobYearCombo.getSelectedItem()
        );
        String startStr = formatDate(
                (Integer) startDayCombo.getSelectedItem(),
                (String) startMonthCombo.getSelectedItem(),
                (Integer) startYearCombo.getSelectedItem()
        );

        Integer idValue = null;
        try {
            idValue = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException ignored) {}

        long paidAmount;
        try {
            paidAmount = parseRupeesToPaisa(paidAmountField.getText());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }

        boolean isPremium = premiumRadio.isSelected();
        String referral = isPremium ? null : referralField.getText().trim();
        String trainer = isPremium ? trainerField.getText().trim() : null;
        String plan = isPremium ? null : ((model.Plan) Objects.requireNonNull(planCombo.getSelectedItem())).name();

        return new MemberFormData(
                idValue,
                nameField.getText().trim(),
                locationField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                getSelectedGender(),
                dobStr,
                startStr,
                isPremium,
                referral,
                trainer,
                plan,
                paidAmount
        );
    }

    /**
     * Exits Edit mode and returns to Add mode.
     * Resets the submit button text and re-enables all fields.
     * Clears the form so the user can add a new member.
     */
    public void exitEditMode() {
        isEditMode = false;
        submitButton.setText("Add Member");
        enableAllFields();
        clearForm();
    }

    /**
     * Resets the form to a blank Add state.
     */
    public void clearForm() {
        idField.setText("");
        nameField.setText("");
        locationField.setText("");
        phoneField.setText("");
        emailField.setText("");
        maleRadio.setSelected(true);
        dobDayCombo.setSelectedIndex(0);
        dobMonthCombo.setSelectedIndex(0);
        dobYearCombo.setSelectedIndex(0);
        startDayCombo.setSelectedIndex(0);
        startMonthCombo.setSelectedIndex(0);
        startYearCombo.setSelectedIndex(0);
        regularRadio.setSelected(true);
        referralField.setText("");
        referralField.setVisible(true);
        trainerField.setText("");
        trainerField.setVisible(false);
        planCombo.setSelectedIndex(0);
        planCombo.setEnabled(true);
        paidAmountField.setText("");
        isEditMode = false;
        submitButton.setText("Add Member");
        enableAllFields();
    }

    /**
     * Sets the form to Regular member mode.
     * Shows Referral Source field, hides Personal Trainer field.
     * Enables Plan combo box.
     * Does NOT clear any existing form data.
     */
    public void setModeRegular() {
        regularRadio.setSelected(true);
        premiumRadio.setSelected(false);

        referralField.setVisible(true);
        trainerField.setVisible(false);
        trainerField.setText("");

        planCombo.setEnabled(true);

        revalidate();
        repaint();
    }

    /**
     * Sets the form to Premium member mode.
     * Shows Personal Trainer field, hides Referral Source field.
     * Disables Plan combo box.
     * Does NOT clear any existing form data.
     */
    public void setModePremium() {
        premiumRadio.setSelected(true);
        regularRadio.setSelected(false);

        trainerField.setVisible(true);
        referralField.setVisible(false);
        referralField.setText("");

        planCombo.setEnabled(false);

        revalidate();
        repaint();
    }

    /**
     * Populates form with existing member data (for Edit mode).
     */
    public void populateFromMember(model.GymMember member) {
        isEditMode = true;
        submitButton.setText("Save Changes");

        idField.setText(String.valueOf(member.getId()));
        nameField.setText(member.getName());
        locationField.setText(member.getLocation());
        phoneField.setText(member.getPhone());
        emailField.setText(member.getEmail());
        setGender(member.getGender());
        setDateCombo(member.getDateOfBirth(), dobDayCombo, dobMonthCombo, dobYearCombo);
        setDateCombo(member.getMembershipStartDate(), startDayCombo, startMonthCombo, startYearCombo);

        if (member instanceof model.RegularMember rm) {
            regularRadio.setSelected(true);
            referralField.setText(rm.getReferralSource());
            referralField.setVisible(true);
            trainerField.setVisible(false);
            planCombo.setSelectedItem(rm.getCurrentPlan());
            planCombo.setEnabled(true);
        } else if (member instanceof model.PremiumMember pm) {
            premiumRadio.setSelected(true);
            trainerField.setText(pm.getPersonalTrainer());
            referralField.setVisible(false);
            trainerField.setVisible(true);
            planCombo.setEnabled(false);
        }

        paidAmountField.setText("");

        disableEditModeFields();
    }

    /**
     * Exposes submit button for controller attachment.
     */
    public JButton getSubmitButton() {
        return submitButton;
    }

    /**
     * Returns true if currently in Edit mode (vs Add mode).
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    // ========== Helper Methods ==========

    private Integer[] createDayArray() {
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    private String[] createMonthArray() {
        return new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
    }

    private String formatDate(Integer day, String month, Integer year) {
        return String.format("%02d-%s-%d", day, month, year);
    }

    private void setDateCombo(String dateStr, JComboBox<Integer> dayCombo,
                              JComboBox<String> monthCombo, JComboBox<Integer> yearCombo) {
        String[] parts = dateStr.split("-");
        if (parts.length == 3) {
            dayCombo.setSelectedItem(Integer.parseInt(parts[0]));
            monthCombo.setSelectedItem(parts[1]);
            yearCombo.setSelectedItem(Integer.parseInt(parts[2]));
        }
    }

    private String getSelectedGender() {
        if (maleRadio.isSelected()) return "Male";
        if (femaleRadio.isSelected()) return "Female";
        if (otherRadio.isSelected()) return "Other";
        return "Prefer not to say";
    }

    private void setGender(String gender) {
        switch (gender) {
            case "Male" -> maleRadio.setSelected(true);
            case "Female" -> femaleRadio.setSelected(true);
            case "Other" -> otherRadio.setSelected(true);
            case "Prefer not to say" -> prefersRadio.setSelected(true);
        }
    }

    private void disableEditModeFields() {
        idField.setEnabled(false);
        dobDayCombo.setEnabled(false);
        dobMonthCombo.setEnabled(false);
        dobYearCombo.setEnabled(false);
        startDayCombo.setEnabled(false);
        startMonthCombo.setEnabled(false);
        startYearCombo.setEnabled(false);
        regularRadio.setEnabled(false);
        premiumRadio.setEnabled(false);
        planCombo.setEnabled(false);
    }

    private void enableAllFields() {
        idField.setEnabled(true);
        dobDayCombo.setEnabled(true);
        dobMonthCombo.setEnabled(true);
        dobYearCombo.setEnabled(true);
        startDayCombo.setEnabled(true);
        startMonthCombo.setEnabled(true);
        startYearCombo.setEnabled(true);
        regularRadio.setEnabled(true);
        premiumRadio.setEnabled(true);
        if (regularRadio.isSelected()) {
            planCombo.setEnabled(true);
        }
    }
}