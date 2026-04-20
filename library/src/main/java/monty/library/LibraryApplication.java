package monty.library;

import javax.swing.*;

public class LibraryApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryGUI().setVisible(true));
    }

}
