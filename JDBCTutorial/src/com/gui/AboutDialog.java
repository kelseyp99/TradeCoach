package com.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Font;

public class AboutDialog extends JDialog {
  public AboutDialog(JFrame parent) {
    super(parent, "About Dialog", true);
    setTitle("About");
    setIconImage(Toolkit.getDefaultToolkit().getImage(AboutDialog.class.getResource("/com/gui/images/hat-bowlhat-icon.png")));

    Box b = Box.createVerticalBox();
    
    JPanel panel = new JPanel();
    FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
    flowLayout_1.setAlignment(FlowLayout.RIGHT);
    b.add(panel);
    
    JLabel lblNewLabel = new JLabel("");
    panel.add(lblNewLabel);
    lblNewLabel.setIcon(new ImageIcon(AboutDialog.class.getResource("/com/gui/images/hat-bowlhat-icon (1).png")));
    
    JPanel panel_1 = new JPanel();
    panel.add(panel_1);
    FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
    flowLayout.setAlignment(FlowLayout.LEFT);
    Component glue = Box.createGlue();
    panel_1.add(glue);
    JLabel label = new JLabel("TradeCoach");
    label.setFont(new Font("Tahoma", Font.BOLD, 14));
    panel_1.add(label);
    JLabel lblVersion = new JLabel("Version 1.0  ");
    lblVersion.setFont(new Font("Tahoma", Font.ITALIC, 11));
    panel_1.add(lblVersion);
    Component glue_1 = Box.createGlue();
    panel_1.add(glue_1);
    
    JLabel lblByPhilKelsey = new JLabel("by Phil Kelsey, CPA/CITP MCS    ");
    panel.add(lblByPhilKelsey);
    getContentPane().add(b, BorderLayout.CENTER);

    JPanel p2 = new JPanel();
    JButton ok = new JButton("Ok");
    p2.add(ok);
    getContentPane().add(p2, "South");

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        setVisible(false);
      }
    });

    setSize(250, 150);
  }

  public static void main(String[] args) {
    JDialog f = new AboutDialog(new JFrame());
    f.show();
  }
}
           
         
    