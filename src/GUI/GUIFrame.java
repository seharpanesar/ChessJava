package GUI;

import javax.swing.*;

public class GUIFrame extends JFrame{
    public GUIFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.pack();
        this.setLocationRelativeTo(null); // frame appears incenter of screen
        this.setVisible(true);
    }
}
