package com.dexels.navajo.tipi.components.swingimpl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.dexels.navajo.tipi.TipiContext;
import com.dexels.navajo.tipi.TipiErrorHandler;
import com.dexels.navajo.tipi.internal.BaseTipiErrorHandler;

public class SwingExceptionDialog extends JDialog {
    private static final int MAX_STACK_LINE_COUNT = 10;
    
    // Provides text
    TipiErrorHandler  eHandler;
    
    private static final long serialVersionUID = -5249989486597779365L;
    private int dialogWidth = 500;
    private int dialogHeight = 140;

    private JLabel iconLabel = new JLabel();
    
    private boolean open = false;

    private JLabel errorLabel = new JLabel();

    private JTextArea exceptionTextArea;
    private JScrollPane exceptionTextAreaSP;

    private JButton okButton = new JButton();
    private JButton viewButton = new JButton();

    private JPanel topPanel = new JPanel(new BorderLayout());

  

    public SwingExceptionDialog(JFrame frame,TipiContext tc, String errorMessage) {
      
        super(frame,false);
        eHandler = new BaseTipiErrorHandler();
        eHandler.setContext(tc);
        createDialog(frame, eHandler.getGenericErrorTitle(), eHandler.getGenericErrorDescription(), errorMessage);
    }

    public SwingExceptionDialog(JFrame frame, String title, String errorDescription, Throwable e) {
        super(frame,false);
        eHandler= new BaseTipiErrorHandler();
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        createDialog(frame, title, errorDescription, errors.toString());
    }

    
    
    private void createDialog(JFrame frame, String title, String errorDescription, String errorMessage) {
        setSize(dialogWidth, dialogHeight);
        setErrorLabelText(errorDescription);
        int lines = errorMessage.split("\r\n|\r|\n").length;
        lines = Math.max(3,  lines);
        int height = Math.min(MAX_STACK_LINE_COUNT, lines);
        exceptionTextArea = new JTextArea(height, 40);
        exceptionTextArea.setText(errorMessage);
        exceptionTextArea.setVisible(false);
        exceptionTextArea.setEditable(false);
        exceptionTextAreaSP = new JScrollPane(exceptionTextArea);
        exceptionTextAreaSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        okButton.setText(eHandler.getErrorCloseText());
        viewButton.setText(eHandler.getErrorMoreDetailsText());
        
        iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        this.setTitle(title);
        setupUI(frame);
        setUpListeners();
    }

    public void setupUI(JFrame parentFrame) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(viewButton);
        
        buttonPanel.add(okButton);
       

        errorLabel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        topPanel.add(iconLabel, BorderLayout.WEST);

        JPanel p = new JPanel(new BorderLayout());
        p.add(errorLabel, BorderLayout.NORTH);

        topPanel.add(p);

        this.add(topPanel);

        this.add(buttonPanel, BorderLayout.SOUTH);
        
        setFocusableWindowState(true);
        setMinimumSize(new Dimension(500, 140));
        setResizable(true);
        pack();
        setLocationRelativeTo(parentFrame);
        
    }
    
    private void setErrorLabelText(String text) {
        // multiline needs HTML.....
        String htmlText = String.format("<html><div style=\"width:500px;\">%s</div><html>", text.replace("\n", "<br>"));
        errorLabel.setText(htmlText);
    }

    private void setUpListeners() {

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingExceptionDialog.this.setVisible(false);
            }
        });

        viewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (open) {
                    viewButton.setText("Details");
                    topPanel.remove(exceptionTextAreaSP);
                    SwingExceptionDialog.this.exceptionTextArea.setVisible(false);
                    SwingExceptionDialog.this.pack();
                    open = false;

                } else {

                    viewButton.setText("Verberg details ");

                    topPanel.add(exceptionTextAreaSP, BorderLayout.SOUTH);

                    SwingExceptionDialog.this.exceptionTextArea.setVisible(true);
                    SwingExceptionDialog.this.pack();

                    open = true;
                }
            }
        });

    }
    public void display() {
        pack();
        setVisible(true);

    }


}
