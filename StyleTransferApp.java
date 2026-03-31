package com.styletransfer;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-Time Style Transfer Application
 * Applies artistic styles (Van Gogh, Picasso, Monet, etc.) to live webcam feed
 * using pre-trained neural style transfer models via OpenCV DNN module.
 *
 * @author VIT Student Project
 * @version 1.0
 */
public class StyleTransferApp extends JFrame {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    // --- UI Components ---
    private JLabel videoLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;

    // --- CV / DNN ---
    private VideoCapture camera;
    private Net styleNet;
    private boolean isRunning = false;
    private boolean applyStyle = false;

    // --- Style Models ---
    private final String[] STYLE_NAMES = {
        "Original", "Van Gogh", "Picasso", "Monet", "Starry Night", "Candy"
    };
    private final String[] MODEL_FILES = {
        null,
        "models/vangogh.t7",
        "models/picasso.t7",
        "models/monet.t7",
        "models/starry_night.t7",
        "models/candy.t7"
    };
    private int currentStyle = 0;
    private List<JButton> styleButtons = new ArrayList<>();

    // --- Image size fed to model ---
    private static final int IMG_W = 640;
    private static final int IMG_H = 480;

    // ------------------------------------------------------------------
    public StyleTransferApp() {
        super("Real-Time Style Transfer | VIT Project");
        initUI();
        initCamera();
    }

    // ------------------------------------------------------------------
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(30, 30, 30));

        // --- Top: title ---
        JLabel title = new JLabel("  🎨 Real-Time Neural Style Transfer", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        add(title, BorderLayout.NORTH);

        // --- Centre: webcam feed ---
        videoLabel = new JLabel();
        videoLabel.setPreferredSize(new Dimension(IMG_W, IMG_H));
        videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        JPanel videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(new Color(30, 30, 30));
        videoPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 2));
        videoPanel.add(videoLabel, BorderLayout.CENTER);
        add(videoPanel, BorderLayout.CENTER);

        // --- Right: style selector ---
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(40, 40, 40));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel styleLabel = new JLabel("Select Style");
        styleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        styleLabel.setForeground(new Color(200, 200, 255));
        styleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(styleLabel);
        controlPanel.add(Box.createVerticalStrut(12));

        Color[] BUTTON_COLORS = {
            new Color(80, 80, 80),   // Original
            new Color(30, 80, 140),  // Van Gogh – blue
            new Color(140, 40, 40),  // Picasso – red
            new Color(40, 120, 60),  // Monet – green
            new Color(60, 30, 120),  // Starry Night – purple
            new Color(180, 100, 20)  // Candy – orange
        };

        for (int i = 0; i < STYLE_NAMES.length; i++) {
            final int idx = i;
            JButton btn = new JButton(STYLE_NAMES[i]);
            btn.setFont(new Font("Arial", Font.PLAIN, 13));
            btn.setForeground(Color.WHITE);
            btn.setBackground(BUTTON_COLORS[i]);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(160, 40));
            btn.addActionListener(e -> switchStyle(idx));
            styleButtons.add(btn);
            controlPanel.add(btn);
            controlPanel.add(Box.createVerticalStrut(6));
        }

        controlPanel.add(Box.createVerticalStrut(20));

        // FPS counter placeholder
        statusLabel = new JLabel("FPS: --");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 255, 150));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(statusLabel);

        add(controlPanel, BorderLayout.EAST);

        // --- Bottom: shortcuts hint ---
        JLabel hint = new JLabel("  Keys: [1-6] Switch style  |  [S] Screenshot  |  [Q] Quit");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(new Color(140, 140, 140));
        hint.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        add(hint, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= '1' && c <= '6') switchStyle(c - '1');
                else if (c == 's' || c == 'S') saveScreenshot();
                else if (c == 'q' || c == 'Q') shutdown();
            }
        });

        highlightActiveButton(0);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ------------------------------------------------------------------
    private void initCamera() {
        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            JOptionPane.showMessageDialog(this,
                "Could not open webcam.\nMake sure a camera is connected.",
                "Camera Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        camera.set(3, IMG_W);
        camera.set(4, IMG_H);
        isRunning = true;
        new Thread(this::captureLoop, "camera-thread").start();
    }

    // ------------------------------------------------------------------
    private void captureLoop() {
        Mat frame = new Mat();
        long lastTime = System.currentTimeMillis();
        int frameCount = 0;

        while (isRunning) {
            if (!camera.read(frame) || frame.empty()) continue;

            Mat display;
            if (applyStyle && styleNet != null) {
                display = applyStyleTransfer(frame);
            } else {
                display = frame.clone();
            }

            // Flip horizontally for mirror effect
            Core.flip(display, display, 1);

            // Convert to BufferedImage and push to UI
            BufferedImage img = matToBufferedImage(display);
            SwingUtilities.invokeLater(() -> {
                videoLabel.setIcon(new ImageIcon(img));
                videoLabel.repaint();
            });

            // FPS
            frameCount++;
            long now = System.currentTimeMillis();
            if (now - lastTime >= 1000) {
                int fps = frameCount;
                frameCount = 0;
                lastTime = now;
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("FPS: " + fps + "  |  Style: " + STYLE_NAMES[currentStyle]));
            }

            display.release();
        }
        frame.release();
        camera.release();
    }

    // ------------------------------------------------------------------
    /**
     * Performs neural style transfer using the loaded .t7 Torch model via OpenCV DNN.
     * Pre-processes: resize → subtract mean → create blob.
     * Post-processes: clip to [0,255] → convert back to BGR Mat.
     */
    private Mat applyStyleTransfer(Mat src) {
        try {
            Mat resized = new Mat();
            Imgproc.resize(src, resized, new Size(IMG_W, IMG_H));

            // Build blob: scale 1.0, subtract ImageNet mean
            Mat blob = Dnn.blobFromImage(resized, 1.0,
                new Size(IMG_W, IMG_H),
                new Scalar(103.939, 116.779, 123.680), false, false);

            styleNet.setInput(blob);
            Mat output = styleNet.forward();

            // output shape: [1, 3, H, W] — reshape to [3, H, W]
            int[] shape = { 3, IMG_H, IMG_W };
            output = output.reshape(1, shape);

            // Split into channels
            List<Mat> channels = new ArrayList<>();
            for (int c = 0; c < 3; c++) {
                Mat ch = output.row(c).reshape(1, IMG_H);
                // Add mean back
                Core.add(ch, new Scalar(new double[]{
                    c == 0 ? 103.939 : c == 1 ? 116.779 : 123.680}), ch);
                channels.add(ch);
            }

            Mat result = new Mat();
            Core.merge(channels, result);
            result.convertTo(result, CvType.CV_8UC3);

            // Clip values
            Core.min(result, new Scalar(255, 255, 255), result);
            Core.max(result, new Scalar(0, 0, 0), result);

            blob.release();
            resized.release();
            output.release();
            return result;

        } catch (Exception ex) {
            System.err.println("Style transfer error: " + ex.getMessage());
            return src.clone();
        }
    }

    // ------------------------------------------------------------------
    private void switchStyle(int idx) {
        if (idx < 0 || idx >= STYLE_NAMES.length) return;
        currentStyle = idx;

        if (idx == 0) {
            applyStyle = false;
            styleNet = null;
        } else {
            String modelPath = MODEL_FILES[idx];
            File f = new File(modelPath);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this,
                    "Model not found: " + modelPath + "\n" +
                    "Please download and place in the models/ directory.\n" +
                    "See README for download instructions.",
                    "Model Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }
            statusLabel.setText("Loading model...");
            new Thread(() -> {
                Net net = Dnn.readNetFromTorch(modelPath);
                SwingUtilities.invokeLater(() -> {
                    styleNet = net;
                    applyStyle = true;
                    statusLabel.setText("Style: " + STYLE_NAMES[idx]);
                });
            }).start();
        }
        highlightActiveButton(idx);
    }

    // ------------------------------------------------------------------
    private void highlightActiveButton(int idx) {
        for (int i = 0; i < styleButtons.size(); i++) {
            styleButtons.get(i).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    i == idx ? Color.WHITE : new Color(80, 80, 80), i == idx ? 2 : 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        }
    }

    // ------------------------------------------------------------------
    private void saveScreenshot() {
        if (videoLabel.getIcon() == null) return;
        try {
            BufferedImage img = new BufferedImage(
                videoLabel.getWidth(), videoLabel.getHeight(), BufferedImage.TYPE_INT_RGB);
            videoLabel.paint(img.getGraphics());
            String fname = "screenshot_" + System.currentTimeMillis() + ".png";
            javax.imageio.ImageIO.write(img, "PNG", new File(fname));
            JOptionPane.showMessageDialog(this, "Saved: " + fname);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    private void shutdown() {
        isRunning = false;
        dispose();
        System.exit(0);
    }

    // ------------------------------------------------------------------
    /** Convert OpenCV Mat (BGR) to Java BufferedImage (RGB). */
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = mat.channels() == 1
            ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels =
            ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StyleTransferApp::new);
    }
}
