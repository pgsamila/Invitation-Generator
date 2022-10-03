import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InvitationGeneratorApp {
    private JTextField sourceFileTextField;
    private JButton openSourceFileButton;
    private JTextField targetFolderTextField;
    private JButton openTargetFolderButton;
    private JButton generateInvitationsButton;
    private JPanel controlPanel;
    private JComboBox fontComboBox;
    private JLabel fontPreview;
    private JComboBox fontStyleComboBox;
    private JSpinner fontSizeSpinner;
    private JTextField nameListTextField;
    private JButton openNameListFileButton;
    private JProgressBar progressBar;
    private JLabel statusText;
    private JLabel imagePrevLabel;
    private JButton selectColorButton;
    private JPanel colorPlane;
    private JLabel xValLabel;
    private JLabel yValLabel;
    private JSpinner spinnerY;
    private JSpinner spinnerX;
    private JSlider sliderPreviewScale;
    private JButton exportFormatSettingsButton;
    private JButton importFormatSettingsButton;
    private JFrame mainFrame;
    private InvitationGenerator invitationGenerator;


    public InvitationGeneratorApp(){
        invitationGenerator = new InvitationGenerator();
        prepareGUI();
    }

    public static void main(String[] args){
        InvitationGeneratorApp invitationGeneratorApp = new InvitationGeneratorApp();
        invitationGeneratorApp.initialize();
    }

    public void initialize(){
        sourceFileTextField.setText(invitationGenerator.getSourceFileLocation());
        targetFolderTextField.setText(invitationGenerator.getTargetFolderLocation());
        nameListTextField.setText(invitationGenerator.getNameListFile());
        fontSizeSpinner.setValue(invitationGenerator.getFontSize());
        fontComboBox.setSelectedItem(invitationGenerator.getFont());
        fontStyleComboBox.setSelectedItem("Plain");
        loadPreview();
        colorPlane.setBackground(invitationGenerator.getFontColor());
        sliderPreviewScale.setValue(invitationGenerator.getScale());
        sliderPreviewScale.setMaximum(10);
        sliderPreviewScale.setMinimum(1);
        loadValues();
        mainFrame.setVisible(true);
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Invitation Generator - 1.0v");
        mainFrame.setSize(750,900);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        mainFrame.add(controlPanel);

        openSourceFileButton.addActionListener(e -> {
            JFileChooser chooser= new JFileChooser();
            chooser.setDialogTitle("Select Source Image");
            FileNameExtensionFilter filter=new FileNameExtensionFilter("Image Files","jpg","jpeg","png","gif");
            chooser.setFileFilter(filter);
            JLabel img=new JLabel();
            img.setPreferredSize(new Dimension(300,300));
            chooser.setAccessory(img);
            chooser.addPropertyChangeListener(pe -> {
                SwingWorker<Image, Void> worker = new SwingWorker<Image, Void>() {
                    protected Image doInBackground() {
                        if (pe.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                            File f = chooser.getSelectedFile();
                            try {
                                FileInputStream fin = new FileInputStream(f);
                                BufferedImage bim = ImageIO.read(fin);
                                return bim.getScaledInstance(178, 170, BufferedImage.SCALE_FAST);
                            } catch (Exception e1) {
                                img.setText(" Not valid image/Unable to read");
                            }
                        }
                        return null;
                    }
                    protected void done() {
                        try {
                            Image i = get(1L, TimeUnit.NANOSECONDS);
                            if (i == null) return;
                            img.setIcon(new ImageIcon(i));
                            img.setText("");
                        } catch (Exception e1) {
                            img.setText(" Error occurred.");
                        }
                    }
                };
                worker.execute();
            });

            chooser.showOpenDialog(null);

            File f= chooser.getSelectedFile();
            String filename= f.getAbsolutePath();
            invitationGenerator.setSourceFileLocation(filename);
            sourceFileTextField.setText(invitationGenerator.getSourceFileLocation());
            loadValues();
            loadPreview();
        });

        spinnerX.addChangeListener(e -> {
            int value = (int) spinnerX.getValue();
            invitationGenerator.setXPosition(value);
            loadPreview();
        });

        spinnerY.addChangeListener(e -> {
            int value = (int) spinnerY.getValue();
            invitationGenerator.setYPosition(value);
            loadPreview();
        });

        sliderPreviewScale.addChangeListener(e -> {
            int value = sliderPreviewScale.getValue();
            invitationGenerator.setScale(value);
            loadPreview();
        });

        selectColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", invitationGenerator.getFontColor());
            if(newColor != null){
                invitationGenerator.setFontColor(newColor);
            }
            loadPreview();
        });

        openTargetFolderButton.addActionListener(e -> {
            JFileChooser chooser= new JFileChooser();
            chooser.setDialogTitle("Select Folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setFileFilter( new FileFilter(){
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }
                @Override
                public String getDescription() {
                    return "Any folder";
                }
            });
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String folder = String.valueOf(chooser.getSelectedFile());
                invitationGenerator.setTargetFolderLocation(folder);
                targetFolderTextField.setText(invitationGenerator.getTargetFolderLocation());
            }
        });

        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontComboBox.setModel(new DefaultComboBoxModel(fonts));
        fontComboBox.addActionListener(e -> {
            if(Objects.equals(e.getActionCommand(), "comboBoxChanged")){
                String font = fonts[fontComboBox.getSelectedIndex()];
                invitationGenerator.setFont(font);
                loadPreview();
            }
        });

        String styles[] = {"Plain", "Bold", "Italic"};
        fontStyleComboBox.setModel(new DefaultComboBoxModel(styles));
        fontStyleComboBox.addActionListener(e -> {
            if(Objects.equals(e.getActionCommand(), "comboBoxChanged")){
                String style = styles[fontStyleComboBox.getSelectedIndex()];
                if(style.equals("Bold")){
                    invitationGenerator.setFontStyle(Font.BOLD);
                }else if(style.equals("Italic")){
                    invitationGenerator.setFontStyle(Font.ITALIC);
                }else {
                    invitationGenerator.setFontStyle(Font.PLAIN);
                }
                loadPreview();
            }
        });

        fontSizeSpinner.addChangeListener(e -> {
            invitationGenerator.setFontSize(Integer.parseInt(fontSizeSpinner.getValue().toString()));
            loadPreview();
        });

        openNameListFileButton.addActionListener(e -> {
            JFileChooser chooser= new JFileChooser();
            chooser.setDialogTitle("Select Names List");
            FileNameExtensionFilter extFilter = new FileNameExtensionFilter("Text file", "txt");
            chooser.setFileFilter(extFilter);
            chooser.showOpenDialog(null);
            File f= chooser.getSelectedFile();
            String filename= f.getAbsolutePath();
            invitationGenerator.setNameListFile(filename);
            nameListTextField.setText(invitationGenerator.getNameListFile());
        });

        generateInvitationsButton.addActionListener(e -> {
            statusText.setText("Generating ...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    generateInvitationsButton.setEnabled(false);
                    try {
                        progressBar.setBackground(Color.GREEN);
                        int nameSize = invitationGenerator.processNames();
                        progressBar.setMaximum(nameSize + 10);
                        progressBar.setMinimum(0);
                        progressBar.setValue(10);
                        invitationGenerator.createImages(progressBar);
                        statusText.setText("Done!");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        statusText.setText("Failed! : "+ex.getMessage());
                        progressBar.setForeground(Color.RED);
                    }
                    generateInvitationsButton.setEnabled(true);
                }
            }).start();

        });

        exportFormatSettingsButton.addActionListener(e ->{
            JSONObject obj = new JSONObject();
            obj.put("fontName", invitationGenerator.getFont());
            obj.put("fontSize", invitationGenerator.getFontSize());
            obj.put("fontStyle", invitationGenerator.getFontStyle());
            obj.put("fontColor",invitationGenerator.getFontColor().getRGB());
            obj.put("textXPosition", invitationGenerator.getTextPositionX());
            obj.put("textYPosition", invitationGenerator.getTextPositionY());
            JFrame parentFrame = new JFrame();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to Export Text settings");
            int userSelection = fileChooser.showSaveDialog(parentFrame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                FileWriter file = null;
                try {
                    try {
                        file = new FileWriter(fileToSave.getAbsolutePath()+".json");
                        file.write(obj.toJSONString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }finally {
                    try {
                        if (file != null) {
                            file.flush();
                            file.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        importFormatSettingsButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Text Format Settings File");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Json Files", "json");
            chooser.setFileFilter(filter);
            chooser.showOpenDialog(null);

            File textFormatFile = chooser.getSelectedFile();

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(textFormatFile)) {
                JSONObject obj = (JSONObject) jsonParser.parse(reader);
                String fontName = (String) obj.get("fontName");
                Long fontSize = (Long) obj.get("fontSize");
                Long fontStyle = (Long) obj.get("fontStyle");
                Long fontColor = (Long) obj.get("fontColor");
                Long textXPosition = (Long) obj.get("textXPosition");
                Long textYPosition = (Long) obj.get("textYPosition");
                int rgb = fontColor.intValue();
                Color color = new Color(rgb);

                // Load save data
                invitationGenerator.setFont(fontName);
                invitationGenerator.setFontSize(Math.toIntExact(fontSize));
                invitationGenerator.setFontStyle(Math.toIntExact(fontStyle));
                invitationGenerator.setFontColor(color);
                invitationGenerator.setXPosition(Math.toIntExact(textXPosition));
                invitationGenerator.setYPosition(Math.toIntExact(textYPosition));

                // Load UI
                fontComboBox.setSelectedItem(fontName);
                fontSizeSpinner.setValue(fontSize);
                fontStyleComboBox.setSelectedItem(invitationGenerator.getFontStyle());
                colorPlane.setBackground(color);
                spinnerX.setValue(Math.toIntExact(textXPosition));
                spinnerY.setValue(Math.toIntExact(textYPosition));
            } catch (IOException | ParseException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            // Update preview
            loadPreview();
        });
    }

    private void loadValues() {
        spinnerX.setValue(invitationGenerator.getImageWidth()/2);
        spinnerY.setValue(invitationGenerator.getImageHeight()/2);
        yValLabel.setText("Vertical direction min: 0 & max: "+invitationGenerator.getImageHeight());
        xValLabel.setText("Horizontal direction min: 0 & max: "+invitationGenerator.getImageWidth());
    }

    private void loadPreview() {
        invitationGenerator.loadPreview(imagePrevLabel);
        colorPlane.setBackground(invitationGenerator.getFontColor());
    }
}
