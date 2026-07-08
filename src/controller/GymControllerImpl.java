package controller;

import model.*;
import service.*;
import view.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GymControllerImpl implements GymController {

    private final service.MemberService memberService;
    private final view.MainFrame mainFrame;
    private final view.MemberTablePanel tablePanel;
    private static final Logger LOGGER = Logger.getLogger(GymControllerImpl.class.getName());
    private final MemberFileService fileService = new MemberFileService();

    public GymControllerImpl(service.MemberService memberService, view.MainFrame mainFrame,
                             view.MemberTablePanel tablePanel) {
        this.memberService = memberService;
        this.mainFrame = mainFrame;
        this.tablePanel = tablePanel;
    }

    @Override
    public void addMember(MemberFormData data) {
        try {
            int id = (data.id() != null) ? data.id()
                    : view.MemberFormPanel.suggestNextId(memberService.getAllMembers());

            GymMember member = getGymMember(data, id);

            memberService.addMember(member);

            if (data.initialPaidAmountPaisa() > 0) {
                PaymentResult result = memberService.payDueAmount(id, data.initialPaidAmountPaisa());
                mainFrame.showInfo("Member added: " + member.getName() + ". " + result.getMessage()
                        + " (currently inactive — use Activate to enable full features)");
            } else {
                mainFrame.showInfo("Member added: " + member.getName()
                        + " (currently inactive — use Activate to enable full features)");
            }

            tablePanel.setFilterSilently("All");

            refreshTable();
            mainFrame.getFormPanel().clearForm();

        } catch (Exception e) {
            mainFrame.showError(e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to add member", e);
        }
    }

    private GymMember getGymMember(MemberFormData data, int id) {
        GymMember member;
        if (data.isPremium()) {
            member = new PremiumMember(id, data.name(), data.location(), data.phone(),
                    data.email(), data.gender(), data.dateOfBirth(), data.membershipStartDate(),
                    data.personalTrainer());
        } else {
            member = new RegularMember(id, data.name(), data.location(), data.phone(),
                    data.email(), data.gender(), data.dateOfBirth(), data.membershipStartDate(),
                    data.referralSource());
        }
        return member;
    }

    @Override
    public void updateMember(int id, MemberFormData data) {
        try {
            String referralSourceOrNull = data.isPremium() ? null : data.referralSource();
            String personalTrainerOrNull = data.isPremium() ? data.personalTrainer() : null;

            memberService.updateMemberDetails(id, data.name(), data.location(), data.phone(),
                    data.email(), data.gender(), referralSourceOrNull, personalTrainerOrNull);

            mainFrame.showInfo("Member updated: " + data.name());
            mainFrame.getFormPanel().exitEditMode();
            refreshTable();
        } catch (IllegalArgumentException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void markAttendance(int id) {
        try {
            memberService.markAttendance(id);
            mainFrame.showInfo("Attendance marked for ID: " + id);
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void upgradePlan(int id, Plan newPlan) {
        try {
            UpgradeResult result = memberService.upgradePlan(id, newPlan);
            mainFrame.showInfo(result.message());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void payDueAmount(int id, long amountPaisa) {
        try {
            PaymentResult result = memberService.payDueAmount(id, amountPaisa);
            mainFrame.showInfo(result.getMessage());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void calculateDiscount(int id) {
        try {
            memberService.calculateDiscount(id);
            mainFrame.showInfo("Discount calculated and applied for ID: " + id);
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void activateMember(int id) {
        try {
            memberService.activateMember(id);
            mainFrame.showInfo("Membership activated for ID: " + id);
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void deactivateMember(int id) {
        try {
            memberService.deactivateMember(id);
            mainFrame.showInfo("Membership deactivated for ID: " + id);
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void softDeleteMember(int id, String reason) {
        try {
            memberService.softDeleteMember(id, reason);
            mainFrame.showInfo("Member removed: " + id + " (Reason: " + reason + ")");
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void restoreMember(int id) {
        try {
            memberService.restoreMember(id);
            mainFrame.showInfo("Member restored: " + id + " (currently inactive — use Activate)");
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mainFrame.showError(e.getMessage());
        }
    }

    @Override
    public void refreshTable() {
        String filter = tablePanel.getSelectedFilter();
        List<GymMember> data = switch (filter) {
            case "Active" -> memberService.getActiveMembers();
            case "Removed" -> memberService.getRemovedMembers();
            default -> memberService.getAllMembers();
        };
        tablePanel.setMembers(data);
    }

    @Override
    public void saveData() throws IOException {
        List<GymMember> allMembers = memberService.getAllMembers();
        if (allMembers.isEmpty()) {
            mainFrame.showInfo("No members to save. Add members first.");
            return;
        }
        fileService.saveMembers(allMembers);
        mainFrame.showInfo("Data saved successfully.");
    }

    @Override
    public void loadData() throws IOException {
        // Clear existing members
        memberService.clearAllMembers();

        List<GymMember> loadedMembers = fileService.loadMembers();

        for (GymMember member : loadedMembers) {
            memberService.addMember(member);
        }

        // "ALL" FILTER SO INACTIVE MEMBERS ARE VISIBLE
        tablePanel.setFilterSilently("All");
        refreshTable();
    }

    @Override
    public List<GymMember> getMembers() {
        return memberService.getAllMembers();
    }

    @Override
    public GymMember findMemberById(int id) {
        return memberService.findMemberById(id).orElse(null);
    }

}