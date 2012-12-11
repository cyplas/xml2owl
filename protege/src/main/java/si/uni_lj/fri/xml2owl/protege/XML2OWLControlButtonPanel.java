package si.uni_lj.fri.xml2owl.protege;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
 
/** A panel containing buttons for carrying out mappings and undoing them. */
public class XML2OWLControlButtonPanel extends JPanel implements ActionListener {

    /** The parent component and controller. */
    XML2OWLComponent controller;

    /** A button for applying the mapping. */ 
    JButton mapButton;

    /** A button for undoing the last mapping. */
    JButton undoButton;
 
    /** Constructor. */
    XML2OWLControlButtonPanel(XML2OWLComponent controller) {

        this.controller = controller;

        setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Control"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
        setLayout(new FlowLayout());

        mapButton = new JButton("Map");
        mapButton.addActionListener(this);
        add(mapButton);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        undoButton.setEnabled(false);
        add(undoButton);
    }

    /** Event handler for the Map and Undo buttons. */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Map")) {
            controller.mapRules();
            undoButton.setEnabled(true);
        } else if (command.equals("Undo")) {
            controller.undoOwl();
            undoButton.setEnabled(false);
        }
    }

}