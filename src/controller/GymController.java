package controller;

import model.GymMember;
import model.Plan;
import view.MemberFormData;
import java.io.IOException;
import java.util.List;

/**
 * Interface for the Gym Management System controller.
 * Defines all operations that can be triggered from the View layer.
 */
public interface GymController {
    void addMember(MemberFormData data);
    void updateMember(int id, MemberFormData data);

    void markAttendance(int id);
    void upgradePlan(int id, Plan newPlan);

    void payDueAmount(int id, long amountPaisa);
    void calculateDiscount(int id);

    void activateMember(int id);
    void deactivateMember(int id);

    void softDeleteMember(int id, String reason);
    void restoreMember(int id);

    void refreshTable();

    void saveData() throws IOException;
    void loadData() throws IOException;

    List<GymMember> getMembers();

    /**
     * Finds a member by ID.
     * @param id The member ID to search for
     * @return The GymMember if found, null otherwise
     */
    GymMember findMemberById(int id);
}