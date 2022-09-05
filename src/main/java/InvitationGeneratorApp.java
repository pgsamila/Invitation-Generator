import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private JSlider xSlider;
    private JSlider ySlider;
    private JLabel imagePrevLabel;
    private JButton selectColorButton;
    private JPanel colorPlane;
    private JLabel xValLabel;
    private JLabel yValLabel;
    private JFrame mainFrame;
    private InvitationGenerator invitationGenerator;


    public InvitationGeneratorApp(){
        invitationGenerator = new InvitationGenerator();
        prepareGUI();
    }

    public static void main(String[] args){
        InvitationGeneratorApp swingControlDemo = new InvitationGeneratorApp();
        swingControlDemo.initialize();
    }

    private void initialize(){
        sourceFileTextField.setText(invitationGenerator.getSourceFileLocation());
        targetFolderTextField.setText(invitationGenerator.getTargetFolderLocation());
        nameListTextField.setText(invitationGenerator.getNameListFile());
        fontSizeSpinner.setValue(invitationGenerator.getFontSize());
        fontComboBox.setSelectedItem(invitationGenerator.getFont());
        fontStyleComboBox.setSelectedItem("Plain");
        loadPreview();
        ySlider.setMaximum(invitationGenerator.getImageHeight());
        xSlider.setMaximum(invitationGenerator.getImageWidth());
        xValLabel.setText("X : "+invitationGenerator.getImageWidth());
        yValLabel.setText("Y : "+invitationGenerator.getImageHeight());
        colorPlane.setBackground(invitationGenerator.getFontColor());
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
            xSlider.setMaximum(invitationGenerator.getImageWidth());
            ySlider.setMaximum(invitationGenerator.getImageHeight());
            loadPreview();
        });

        xSlider.addChangeListener(e -> {
            int value = xSlider.getValue();
            invitationGenerator.setXPosition(value);
            xValLabel.setText("X : "+value);
            loadPreview();
        });

        ySlider.addChangeListener(e -> {
            int value = ySlider.getValue();
            invitationGenerator.setYPosition(value);
            yValLabel.setText("Y : "+value);
            loadPreview();
        });

        selectColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", invitationGenerator.getFontColor());
            if(newColor != null){
                invitationGenerator.setFontColor(newColor);
            }
            previewFontStyle();
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
                fontComboBox.setFont(new Font(font,Font.PLAIN,12));
                previewFontStyle();
                fontPreview.setText(font);
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
                previewFontStyle();
                loadPreview();
            }
        });

        fontSizeSpinner.addChangeListener(e -> {
            invitationGenerator.setFontSize(Integer.parseInt(fontSizeSpinner.getValue().toString()));
            previewFontStyle();
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
        });
    }

    private void loadPreview() {
        invitationGenerator.loadPreview(imagePrevLabel);
        colorPlane.setBackground(invitationGenerator.getFontColor());
    }

    private void previewFontStyle() {
        fontPreview.setFont(new Font(invitationGenerator.getFont(),invitationGenerator.getFontStyle(),invitationGenerator.getFontSize()));
    }

}