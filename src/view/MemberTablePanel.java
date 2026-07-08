package view;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Table displaying all gym members with filtering by status.
 */
public class MemberTablePanel extends JPanel {

    private final JComboBox<String> filterCombo;
    private final JTable table;
    private final MemberTableModel tableModel;
    private GymControllerCallback controller;

    /**
     * When true, filterCombo's actionListener will not notify the controller.
     * Used by setFilterSilently() so programmatic filter changes don't trigger
     * a redundant refresh cycle on top of the caller's own refreshTable() call.
     */
    private boolean suppressFilterEvents = false;

    public interface GymControllerCallback {
        void onMemberSelected(model.GymMember memberOrNull);
        void onFilterChanged();
    }

    public MemberTablePanel() {
        setLayout(new BorderLayout());

        // Filter dropdown
        filterCombo = new JComboBox<>(new String[]{"Active", "Removed", "All"});
        filterCombo.setSelectedItem("Active");
        filterCombo.addActionListener(_ -> {
            if (controller != null && !suppressFilterEvents) {
                controller.onFilterChanged();
            }
        });

        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterCombo);
        add(filterPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new MemberTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(_ -> {
            if (controller != null) {
                int selectedRow = table.getSelectedRow();
                model.GymMember selected = selectedRow >= 0 ? tableModel.getMember(selectedRow) : null;
                controller.onMemberSelected(selected);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets the controller callback for member selection and filter changes.
     */
    public void setControllerCallback(GymControllerCallback callback) {
        this.controller = callback;
    }

    /**
     * Returns the currently selected filter value ("Active", "Removed", or "All").
     */
    public String getSelectedFilter() {
        return (String) filterCombo.getSelectedItem();
    }

    /**
     * Sets the filter dropdown's selected value WITHOUT firing onFilterChanged().
     * Use this when a controller wants to change the visible filter as a side
     * effect of some other action (e.g. switching to "All" right after adding
     * a new inactive member) and will call refreshTable() itself afterward.
     * This avoids a double-refresh: one from the combo box's own action event,
     * and one from the caller's explicit refreshTable() call.
     */
    public void setFilterSilently(String filter) {
        suppressFilterEvents = true;
        try {
            filterCombo.setSelectedItem(filter);
        } finally {
            suppressFilterEvents = false;
        }
    }

    /**
     * Updates the table with a new list of members.
     */
    public void setMembers(List<model.GymMember> members) {
        tableModel.setMembers(members);
    }

    /**
     * Custom table model for displaying members.
     */
    private static class MemberTableModel extends AbstractTableModel {
        private List<model.GymMember> members = new ArrayList<>();
        private final String[] columnNames = {
                "ID", "Name", "Type", "Plan", "Status", "Attendance",
                "Loyalty Points", "Paid Amount", "Fully Paid"
        };

        void setMembers(List<model.GymMember> members) {
            this.members = new ArrayList<>(members);
            fireTableDataChanged();
        }

        model.GymMember getMember(int row) {
            if (row >= 0 && row < members.size()) {
                return members.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return members.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            model.GymMember member = members.get(row);
            return switch (col) {
                case 0 -> member.getId();
                case 1 -> member.getName();
                case 2 -> member.getMemberType();
                case 3 -> {
                    if (member instanceof model.RegularMember rm) {
                        yield rm.getCurrentPlan().getDisplayName();
                    }
                    yield "— (Fixed Rs.50,000)";
                }
                case 4 -> member.getStatusSummary();
                case 5 -> member.getAttendance();
                case 6 -> member.getLoyaltyPoints();
                case 7 -> util.CurrencyFormatter.format(member.getPaidAmountPaisa());
                case 8 -> member.isFullyPaid() ? "Yes" : "No";
                default -> "";
            };
        }
    }
}