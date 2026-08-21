/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.j2se;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.ImageReader;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public final class GUIRunner
extends JFrame {
    private final JLabel imageLabel = new JLabel();
    private final JTextComponent textArea = new JTextArea();

    private GUIRunner() {
        this.textArea.setEditable(false);
        this.textArea.setMaximumSize(new Dimension(400, 200));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(this.imageLabel);
        panel.add(this.textArea);
        this.setTitle("ZXing");
        this.setSize(400, 400);
        this.setDefaultCloseOperation(3);
        this.setContentPane(panel);
        this.setLocationRelativeTo(null);
    }

    public static void main(String[] args2) throws MalformedURLException {
        GUIRunner runner = new GUIRunner();
        runner.setVisible(true);
        runner.chooseImage();
    }

    private void chooseImage() throws MalformedURLException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(this);
        Path file = fileChooser.getSelectedFile().toPath();
        ImageIcon imageIcon = new ImageIcon(file.toUri().toURL());
        this.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight() + 100);
        this.imageLabel.setIcon(imageIcon);
        String decodeText = GUIRunner.getDecodeText(file);
        this.textArea.setText(decodeText);
    }

    private static String getDecodeText(Path file) {
        Result result;
        BufferedImage image;
        try {
            image = ImageReader.readImage(file.toUri());
        }
        catch (IOException ioe) {
            return ioe.toString();
        }
        BufferedImageLuminanceSource source2 = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source2));
        try {
            result = new MultiFormatReader().decode(bitmap);
        }
        catch (ReaderException re) {
            return re.toString();
        }
        return String.valueOf(result.getText());
    }
}

