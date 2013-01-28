package si.uni_lj.fri.xml2owl.protege;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.filechooser.*;
 
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

/** A component containing the text field and buttons for the rules or data used in mapping. */
public class XML2OWLTextAreaComponent extends JPanel implements ActionListener {

    /** The parent component and controller. */
    XML2OWLComponent controller;

    /** The title of the component. */
    String title;

    /** The text field. */   
    JTextArea textarea;

    /** A button used for filling the text field with the contents of a file. */ 
    JButton openButton;

    /** A button used to carry out validation (in the case of rules). */
    JButton validateButton;
 
    /** Constructor. */
    XML2OWLTextAreaComponent(XML2OWLComponent controller, String title) {

        this.controller = controller;
        this.title = title;

        setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(title),
                        BorderFactory.createEmptyBorder(5,5,5,5)));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
        JPanel buttonPanel = new JPanel();

        openButton = new JButton("Open");
        openButton.addActionListener(this);
        buttonPanel.add(openButton);
        
        if (title.equals("Rules")) {
            validateButton = new JButton("Validate");
            validateButton.addActionListener(this);
            buttonPanel.add(validateButton);
        }
        
        textarea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textarea);

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
 
    /** Event handler for the Open and Validate buttons. */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Open")) {
            openFile(title);
        } else if (command.equals("Validate")) {
            controller.validateRules();
        }
    }
 
    /** Open a file using a pop-up menu. */
    public void openFile(String title) { 
        System.out.println("openFile" + title); 
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = 
            new FileNameExtensionFilter("XML files", "xml");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                setValue(FileUtils.readFileToString(file));
            } 
            catch (IOException e) {
                controller.showException("File system error", e.getMessage());
            }
        }
    }

    /** Get the current text in the text field. */
    public String getValue() {
        return textarea.getText();
    }

    /** Set the current text in the text field. */
    public void setValue(String text) {
        textarea.setText(text);
    }

}