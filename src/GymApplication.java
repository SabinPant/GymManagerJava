import controller.GymController;
import controller.GymControllerImpl;
import service.MemberService;
import view.MainFrame;
import view.MemberTablePanel;

/**
 * Application entry point for the Gym Management System.
 */
void main() {
    javax.swing.SwingUtilities.invokeLater(() -> {
        MemberService memberService = new MemberService();

        MainFrame mainFrame = new MainFrame(null);
        MemberTablePanel tablePanel = mainFrame.getTablePanel();

        GymController controller = new GymControllerImpl(memberService, mainFrame,
                tablePanel);

        mainFrame.setController(controller);
    });
}