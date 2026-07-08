package service;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handles saving and loading member data to/from CSV file.
 */
public class MemberFileService {

    private static final String FILE_NAME = "members_data.csv";
    private static final String DELIMITER = ",";
    private static final String HEADER = "ID,Name,Location,Phone,Email,Gender,DOB,StartDate,Type,Plan,ReferralSource,PersonalTrainer,PaidAmount,Discount,Attendance,LoyaltyPoints,Active,Removed,RemovalReason,FullyPaid";
    private static final Logger LOGGER = Logger.getLogger(MemberFileService.class.getName());

    /**
     * Saves all members to a CSV file.
     * Only saves if there are members to save.
     */
    public void saveMembers(List<GymMember> members) throws IOException {
        // Don't save if the list is empty — prevent data loss
        if (members == null || members.isEmpty()) {
            throw new IOException("No members to save. Data not overwritten.");
        }

        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (GymMember member : members) {
            lines.add(memberToCsv(member));
        }

        Files.write(Paths.get(FILE_NAME), lines);
    }

    /**
     * Loads members from a CSV file.
     */
    public List<GymMember> loadMembers() throws IOException {
        List<GymMember> members = new ArrayList<>();

        Path filePath = Paths.get(FILE_NAME);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("No saved data found.");
        }

        List<String> lines = Files.readAllLines(filePath);
        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            GymMember member = csvToMember(line);
            if (member != null) {
                members.add(member);
            }
        }

        return members;
    }

    /**
     * Converts a member to CSV string.
     * CSV Format:
     * ID,Name,Location,Phone,Email,Gender,DOB,StartDate,Type,Plan,ReferralSource,
     * PersonalTrainer,PaidAmount,Discount,Attendance,LoyaltyPoints,Active,Removed,
     * RemovalReason,FullyPaid
     */
    private String memberToCsv(GymMember member) {
        List<String> fields = new ArrayList<>();

        fields.add(String.valueOf(member.getId()));
        fields.add(escape(member.getName()));
        fields.add(escape(member.getLocation()));
        fields.add(escape(member.getPhone()));
        fields.add(escape(member.getEmail()));
        fields.add(escape(member.getGender()));
        fields.add(escape(member.getDateOfBirth()));
        fields.add(escape(member.getMembershipStartDate()));
        fields.add(member.getMemberType());

        if (member instanceof RegularMember rm) {
            fields.add(rm.getCurrentPlan().name());
            fields.add(escape(rm.getReferralSource()));
            fields.add(""); // Personal Trainer (empty for Regular)
            fields.add(String.valueOf(rm.getPaidAmountPaisa()));
            fields.add("0"); // Discount (Regular has no discount)
        } else if (member instanceof PremiumMember pm) {
            fields.add("PREMIUM");
            fields.add(""); // Referral Source (empty for Premium)
            fields.add(escape(pm.getPersonalTrainer()));
            fields.add(String.valueOf(pm.getPaidAmountPaisa()));
            fields.add(String.valueOf(pm.getDiscountAmountPaisa()));
        } else {
            fields.add("");
            fields.add("");
            fields.add("");
            fields.add("0");
            fields.add("0");
        }

        fields.add(String.valueOf(member.getAttendance()));
        fields.add(String.valueOf(member.getLoyaltyPoints()));
        fields.add(String.valueOf(member.isActive()));
        fields.add(String.valueOf(member.isRemoved()));
        fields.add(escape(member.getRemovalReason()));
        fields.add(String.valueOf(member.isFullyPaid()));

        return String.join(DELIMITER, fields);
    }

    /**
     * Converts CSV string to a member.
     * CSV Format:
     * 0:ID, 1:Name, 2:Location, 3:Phone, 4:Email, 5:Gender, 6:DOB, 7:StartDate,
     * 8:Type, 9:Plan, 10:ReferralSource, 11:PersonalTrainer, 12:PaidAmount,
     * 13:Discount, 14:Attendance, 15:LoyaltyPoints, 16:Active, 17:Removed,
     * 18:RemovalReason, 19:FullyPaid
     */
    private GymMember csvToMember(String line) {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length < 20) return null;

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = unescape(parts[1].trim());
            String location = unescape(parts[2].trim());
            String phone = unescape(parts[3].trim());
            String email = unescape(parts[4].trim());
            String gender = unescape(parts[5].trim());
            String dob = unescape(parts[6].trim());
            String startDate = unescape(parts[7].trim());
            String type = parts[8].trim();

            GymMember member;
            if ("Premium".equalsIgnoreCase(type)) {
                String trainer = unescape(parts[11].trim());
                member = new PremiumMember(id, name, location, phone, email, gender, dob, startDate, trainer);
            } else {
                String referral = unescape(parts[10].trim());
                member = new RegularMember(id, name, location, phone, email, gender, dob, startDate, referral);
                RegularMember rm = (RegularMember) member;
                String planName = parts[9].trim();
                Plan plan = Plan.fromString(planName).orElse(Plan.BASIC);
                if (plan != Plan.BASIC) {
                    rm.setCurrentPlan(plan);
                }
            }

            // Restore fields in correct order
            long paidAmount = Long.parseLong(parts[12].trim());
            long discount = Long.parseLong(parts[13].trim());
            int attendance = Integer.parseInt(parts[14].trim());
            int loyaltyPoints = Integer.parseInt(parts[15].trim());
            boolean active = Boolean.parseBoolean(parts[16].trim());
            boolean removed = Boolean.parseBoolean(parts[17].trim());
            String removalReason = unescape(parts[18].trim());
            boolean fullyPaid = Boolean.parseBoolean(parts[19].trim());

            // Use setters to restore state
            member.setPaidAmountPaisa(paidAmount);
            member.setAttendance(attendance);
            member.setLoyaltyPoints(loyaltyPoints);
            member.setFullyPaid(fullyPaid);

            if (active) {
                member.activateMembership();
            }
            if (removed) {
                member.softDelete(removalReason);
            }

            // Restore discount for Premium members
            if (member instanceof PremiumMember pm && discount > 0) {
                pm.setDiscountAmountPaisa(discount);
            }

            return member;

        } catch (Exception e) {
            LOGGER.warning("Error parsing line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String unescape(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }

}